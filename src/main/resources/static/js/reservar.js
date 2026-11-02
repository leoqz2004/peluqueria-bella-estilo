// Asistente de reserva de 4 pasos (P04-P06): servicio -> estilista -> horario -> confirmar.
(function () {
  const root = document.getElementById('wizard');
  if (!root) return;
  const $ = (s) => root.querySelector(s), $$ = (s) => [...root.querySelectorAll(s)];
  const st = { paso: 1, estilista: null, fecha: null, hora: null };
  const dias = ['D', 'L', 'M', 'M', 'J', 'V', 'S'];
  const iso = d => d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
  const soles = n => 'S/ ' + n.toFixed(2);

  const servSel = () => $$('input[name=servicioIds]:checked');
  const totales = () => servSel().reduce((a, c) => ({ min: a.min + +c.dataset.min, precio: a.precio + +c.dataset.precio }), { min: 0, precio: 0 });

  function resumen() {
    const t = totales(), s = servSel();
    $('#res-servicios').textContent = s.length ? s.map(c => c.dataset.nombre).join(', ') : '—';
    $('#res-duracion').textContent = t.min ? t.min + ' minutos' : '—';
    $('#res-estilista').textContent = st.estilista ? st.estilista.nombre : '—';
    $('#res-fecha').textContent = st.fecha && st.hora ? st.fecha + ' · ' + st.hora.slice(0, 5) : '—';
    $('#res-total').textContent = soles(t.precio);
  }

  function filtrarEstilistas() {
    const ids = servSel().map(c => c.value);
    let alguno = false;
    $$('.est-card').forEach(e => {
      const ofrece = e.dataset.servicios.split(',');
      const ok = ids.every(i => ofrece.includes(i));
      e.classList.toggle('hidden', !ok); alguno = alguno || ok;
      if (!ok && st.estilista && st.estilista.id === e.dataset.id) { st.estilista = null; e.classList.remove('sel'); }
    });
    $('#sin-estilistas').classList.toggle('hidden', alguno);
  }

  function mostrar(n) {
    st.paso = n;
    $$('.paso-panel').forEach(p => p.classList.toggle('hidden', +p.dataset.paso !== n));
    $$('.paso').forEach(p => { const k = +p.dataset.paso; p.classList.toggle('on', k === n); p.classList.toggle('hecho', k < n); });
    if (n === 2) filtrarEstilistas();
    if (n === 3) { dibujarDias(); }
    if (n === 4) preparar();
    validar(); resumen();
  }

  function validar() {
    const ok = { 1: servSel().length > 0, 2: !!st.estilista, 3: !!(st.fecha && st.hora), 4: true }[st.paso];
    $$('.btn-sig').forEach(b => b.disabled = !ok);
  }

  function dibujarDias() {
    const caja = $('#dias'); caja.innerHTML = '';
    const hoy = new Date();
    for (let i = 0; i < 14; i++) {
      const d = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate() + i);
      const b = document.createElement('button');
      b.type = 'button'; b.className = 'dia' + (iso(d) === st.fecha ? ' sel' : '');
      b.innerHTML = '<small>' + dias[d.getDay()] + '</small><b>' + d.getDate() + '</b>';
      b.title = d.toLocaleDateString('es-PE', { weekday: 'long', day: 'numeric', month: 'long' });
      b.addEventListener('click', () => { st.fecha = iso(d); st.hora = null; dibujarDias(); cargarHoras(); validar(); resumen(); });
      caja.appendChild(b);
    }
    if (st.fecha) cargarHoras();
  }

  async function cargarHoras() {
    const caja = $('#slots'); caja.innerHTML = '<span class="vacio">Cargando horarios…</span>';
    const q = new URLSearchParams({ estilista: st.estilista.id, fecha: st.fecha });
    servSel().forEach(c => q.append('servicios', c.value));
    const r = await fetch('/reservar/horarios?' + q);
    const horas = r.ok ? await r.json() : [];
    caja.innerHTML = horas.length ? '' : '<span class="vacio">No hay horarios libres ese día. Prueba con otra fecha.</span>';
    horas.forEach(h => {
      const b = document.createElement('button');
      b.type = 'button'; b.className = 'slot' + (h === st.hora ? ' sel' : ''); b.textContent = h.slice(0, 5);
      b.addEventListener('click', () => { st.hora = h; cargarHorasMarcar(); validar(); resumen(); });
      caja.appendChild(b);
    });
  }
  function cargarHorasMarcar() { $$('.slot').forEach(b => b.classList.toggle('sel', b.textContent === st.hora.slice(0, 5))); }

  function preparar() {
    const t = totales(), s = servSel();
    const fin = (() => { const [h, m] = st.hora.split(':'); const x = +h * 60 + +m + t.min; return String(Math.floor(x / 60)).padStart(2, '0') + ':' + String(x % 60).padStart(2, '0'); })();
    $('#conf-servicios').textContent = s.map(c => c.dataset.nombre).join(', ');
    $('#conf-estilista').textContent = st.estilista.nombre;
    $('#conf-fecha').textContent = new Date(st.fecha + 'T00:00').toLocaleDateString('es-PE', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    $('#conf-hora').textContent = st.hora.slice(0, 5) + ' – ' + fin;
    $('#conf-total').textContent = soles(t.precio);
    $('#h-estilista').value = st.estilista.id; $('#h-fecha').value = st.fecha; $('#h-hora').value = st.hora;
    const cont = $('#h-servicios'); cont.innerHTML = '';
    s.forEach(c => { const i = document.createElement('input'); i.type = 'hidden'; i.name = 'servicioIds'; i.value = c.value; cont.appendChild(i); });
  }

  // eventos
  $$('input[name=servicioIds]').forEach(c => c.addEventListener('change', () => {
    c.closest('.opcion').classList.toggle('sel', c.checked);
    st.estilista = null; st.fecha = null; st.hora = null; $$('.est-card').forEach(e => e.classList.remove('sel'));
    validar(); resumen();
  }));
  $$('.est-card').forEach(e => e.addEventListener('click', () => {
    $$('.est-card').forEach(x => x.classList.remove('sel')); e.classList.add('sel');
    st.estilista = { id: e.dataset.id, nombre: e.dataset.nombre }; st.fecha = null; st.hora = null; validar(); resumen();
  }));
  $$('.btn-sig').forEach(b => b.addEventListener('click', () => mostrar(st.paso + 1)));
  $$('.btn-atras').forEach(b => b.addEventListener('click', () => mostrar(st.paso - 1)));

  // servicio preseleccionado desde el catalogo
  $$('input[name=servicioIds]').forEach(c => c.closest('.opcion').classList.toggle('sel', c.checked));
  mostrar(1);
})();
