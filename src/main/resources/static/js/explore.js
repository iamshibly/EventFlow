/* Occavie event discovery: client-side search, filters, sorting and pagination
   over the events rendered by the server (/events). State is mirrored in the URL
   so filtered views can be shared and the back button works. */
(() => {
  const $ = id => document.getElementById(id);
  const input = $('eventSearch'), location_ = $('locationFilter');
  const categorySelect = $('categoryFilter'), priceSelect = $('pricingFilter'), statusSelect = $('eventStatusFilter');
  const dateFrom = $('dateFrom'), dateTo = $('dateTo'), sortSelect = $('sortSelect');
  const grid = $('v1EventGrid'), count = $('resultCount'), empty = $('v1Empty'), pager = $('ovPagination');
  if (!input || !grid) return;

  const PAGE_SIZE = 12;
  const cards = Array.from(grid.querySelectorAll('.ov-event-card'));
  const chips = Array.from(document.querySelectorAll('.ov-category-button'));
  let page = 1;

  // "Free", "From ৳500", "৳1,200" -> number (Free = 0)
  const priceOf = card => {
    const label = (card.dataset.price || '').replace(/,/g, '');
    const m = label.match(/\d+(\.\d+)?/);
    return m ? parseFloat(m[0]) : 0;
  };

  const ensureOption = (select, value) => {
    if (!value) return;
    const match = Array.from(select.options).find(o => o.value.toLowerCase() === value.toLowerCase());
    if (match) { select.value = match.value; return; }
    const option = document.createElement('option');
    option.value = value; option.textContent = value; select.add(option); select.value = value;
  };

  const params = new URLSearchParams(window.location.search);
  input.value = params.get('q') || '';
  location_.value = params.get('location') || '';
  ensureOption(categorySelect, params.get('category'));
  const pick = (select, key, allowed) => { const v = (params.get(key) || '').toUpperCase(); if (allowed.includes(v)) select.value = v; };
  pick(priceSelect, 'pricing', ['ALL', 'FREE', 'PAID']);
  pick(statusSelect, 'status', ['ALL', 'UPCOMING', 'COMPLETED']);
  dateFrom.value = /^\d{4}-\d{2}-\d{2}$/.test(params.get('from') || '') ? params.get('from') : '';
  dateTo.value = /^\d{4}-\d{2}-\d{2}$/.test(params.get('to') || '') ? params.get('to') : '';
  const sorts = Array.from(sortSelect.options).map(o => o.value);
  if (sorts.includes(params.get('sort'))) sortSelect.value = params.get('sort');
  page = Math.max(1, parseInt(params.get('page') || '1', 10) || 1);

  const comparators = {
    'date-asc': (a, b) => (a.dataset.date || '').localeCompare(b.dataset.date || ''),
    'date-desc': (a, b) => (b.dataset.date || '').localeCompare(a.dataset.date || ''),
    'title-asc': (a, b) => (a.dataset.title || '').localeCompare(b.dataset.title || '', undefined, { sensitivity: 'base' }),
    'price-asc': (a, b) => priceOf(a) - priceOf(b),
    'price-desc': (a, b) => priceOf(b) - priceOf(a)
  };

  function pageButton(label, target, { current = false, disabled = false, aria } = {}) {
    const b = document.createElement('button');
    b.type = 'button'; b.textContent = label;
    if (aria) b.setAttribute('aria-label', aria);
    if (current) { b.className = 'is-current'; b.setAttribute('aria-current', 'page'); }
    b.disabled = disabled;
    b.addEventListener('click', () => { page = target; apply(true); });
    return b;
  }

  function renderPager(totalPages) {
    pager.replaceChildren();
    if (totalPages <= 1) { pager.hidden = true; return; }
    pager.hidden = false;
    pager.append(pageButton('Previous', page - 1, { disabled: page === 1, aria: 'Previous page' }));
    const shown = new Set([1, totalPages, page - 1, page, page + 1]);
    let last = 0;
    [...shown].filter(n => n >= 1 && n <= totalPages).sort((a, b) => a - b).forEach(n => {
      if (n - last > 1) { const dots = document.createElement('span'); dots.textContent = '…'; dots.setAttribute('aria-hidden', 'true'); pager.append(dots); }
      pager.append(pageButton(String(n), n, { current: n === page, aria: `Page ${n}` }));
      last = n;
    });
    pager.append(pageButton('Next', page + 1, { disabled: page === totalPages, aria: 'Next page' }));
  }

  function apply(scroll) {
    const search = input.value.trim().toLocaleLowerCase();
    const place = location_.value.trim().toLocaleLowerCase();
    const cat = categorySelect.value.toLocaleLowerCase();
    const pricing = priceSelect.value, status = statusSelect.value;
    const from = dateFrom.value, to = dateTo.value;

    const matches = cards.filter(card => {
      const date = card.dataset.date || '';
      return (!search || (card.dataset.search || '').toLocaleLowerCase().includes(search))
        && (!place || (card.dataset.location || '').toLocaleLowerCase().includes(place))
        && (cat === 'all' || (card.dataset.category || '').toLocaleLowerCase() === cat)
        && (pricing === 'ALL' || card.dataset.pricing === pricing)
        && (status === 'ALL' || card.dataset.status === status)
        && (!from || date >= from) && (!to || date <= to);
    }).sort(comparators[sortSelect.value] || comparators['date-asc']);

    const totalPages = Math.max(1, Math.ceil(matches.length / PAGE_SIZE));
    page = Math.min(page, totalPages);
    const visible = new Set(matches.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE));
    matches.forEach(card => grid.appendChild(card));            // reflect sort order in the DOM
    cards.forEach(card => { card.hidden = !visible.has(card); });

    const start = matches.length ? (page - 1) * PAGE_SIZE + 1 : 0;
    count.textContent = matches.length === 0 ? '0 events'
      : `${start}–${start + visible.size - 1} of ${matches.length} event${matches.length === 1 ? '' : 's'}`;
    if (empty) empty.hidden = matches.length > 0 || cards.length === 0;
    grid.hidden = matches.length === 0;
    renderPager(totalPages);

    chips.forEach(chip => {
      const selected = chip.dataset.category.toLocaleLowerCase() === cat;
      chip.classList.toggle('is-active', selected);
      chip.setAttribute('aria-pressed', String(selected));
    });

    const next = new URLSearchParams();
    if (input.value.trim()) next.set('q', input.value.trim());
    if (location_.value.trim()) next.set('location', location_.value.trim());
    if (categorySelect.value !== 'ALL') next.set('category', categorySelect.value);
    if (pricing !== 'ALL') next.set('pricing', pricing);
    if (status !== 'UPCOMING') next.set('status', status);
    if (from) next.set('from', from);
    if (to) next.set('to', to);
    if (sortSelect.value !== 'date-asc') next.set('sort', sortSelect.value);
    if (page > 1) next.set('page', String(page));
    history.replaceState(null, '', `${window.location.pathname}${next.size ? '?' + next : ''}`);
    if (scroll) document.getElementById('resultsHeading')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  const reset = () => {
    input.value = ''; location_.value = ''; categorySelect.value = 'ALL'; priceSelect.value = 'ALL';
    statusSelect.value = 'UPCOMING'; dateFrom.value = ''; dateTo.value = ''; sortSelect.value = 'date-asc';
    page = 1; apply();
  };
  const filterChanged = () => { page = 1; apply(); };

  let timer;
  [input, location_].forEach(el => el.addEventListener('input', () => { clearTimeout(timer); timer = setTimeout(filterChanged, 150); }));
  [categorySelect, priceSelect, statusSelect, dateFrom, dateTo, sortSelect].forEach(el => el.addEventListener('change', filterChanged));
  chips.forEach(chip => chip.addEventListener('click', () => { categorySelect.value = chip.dataset.category; filterChanged(); }));
  $('v1ClearFilters')?.addEventListener('click', reset);
  $('v1ClearFilters2')?.addEventListener('click', reset);
  apply();
})();
