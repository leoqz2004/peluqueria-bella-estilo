// Utilidades de interfaz: tabs, modales, filtros del catalogo.
document.addEventListener('DOMContentLoaded', () => {
  // Tabs: <div data-tabs> con botones [data-tab] y paneles [data-panel]
  document.querySelectorAll('[data-tabs]').forEach(box => {
    const btns = box.querySelectorAll('[data-tab]');
    const show = id => {
      btns.forEach(b => b.classList.toggle('on', b.dataset.tab === id));
      document.querySelectorAll('[data-panel]').forEach(p => p.classList.toggle('hidden', p.dataset.panel !== id));
    };
    btns.forEach(b => b.addEventListener('click', () => show(b.dataset.tab)));
    if (btns.length) show(btns[0].dataset.tab);
  });

  // Modales: boton [data-open-modal=ID]; copia data-action al formulario y data-* a campos [data-fill=nombre]
  document.querySelectorAll('[data-open-modal]').forEach(btn => btn.addEventListener('click', () => {
    const dlg = document.getElementById(btn.dataset.openModal);
    const form = dlg.querySelector('form');
    if (btn.dataset.action && form) form.action = btn.dataset.action;
    dlg.querySelectorAll('[data-fill]').forEach(el => {
      const v = btn.dataset[el.dataset.fill] ?? '';
      if (el.tagName === 'SELECT' && v === '') return;
      if ('value' in el && el.tagName !== 'SPAN') el.value = v; else el.textContent = v;
    });
    const t = dlg.querySelector('[data-titulo]');
    if (t && btn.dataset.titulo) t.textContent = btn.dataset.titulo;
    dlg.showModal();
  }));
  document.querySelectorAll('[data-close]').forEach(b => b.addEventListener('click', () => b.closest('dialog').close()));

  // Filtro del catalogo (P03)
  const buscador = document.getElementById('buscar-servicio');
  const chips = document.querySelectorAll('[data-filtro]');
  const tarjetas = document.querySelectorAll('.svc-card');
  let cat = 'Todos';
  const aplicar = () => {
    const q = buscador ? buscador.value.toLowerCase() : '';
    tarjetas.forEach(t => {
      const okCat = cat === 'Todos' || t.dataset.cat === cat;
      const okTxt = !q || t.dataset.texto.includes(q);
      t.classList.toggle('hidden', !(okCat && okTxt));
    });
  };
  chips.forEach(c => c.addEventListener('click', () => {
    cat = c.dataset.filtro; chips.forEach(x => x.classList.toggle('on', x === c)); aplicar();
  }));
  if (buscador) buscador.addEventListener('input', aplicar);

  // Reprogramar (modal de Mis citas): al elegir fecha se cargan las horas libres de esa cita
  const fecha = document.getElementById('rep-fecha'), horas = document.getElementById('rep-hora');
  if (fecha && horas) fecha.addEventListener('change', async () => {
    const form = fecha.closest('form');
    const id = form.action.split('/citas/')[1].split('/')[0];
    horas.innerHTML = '<option value="">Cargando…</option>';
    const r = await fetch('/citas/' + id + '/horarios?fecha=' + fecha.value);
    const lista = r.ok ? await r.json() : [];
    horas.innerHTML = lista.length ? '' : '<option value="">Sin horarios libres</option>';
    lista.forEach(h => horas.add(new Option(h.substring(0, 5), h)));
  });
});
