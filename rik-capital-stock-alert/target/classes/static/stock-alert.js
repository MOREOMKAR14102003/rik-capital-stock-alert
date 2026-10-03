const searchInput = document.getElementById('companySearch');
const results = document.getElementById('results');
const selectedBox = document.getElementById('selected');
const form = document.getElementById('alertForm');
const statusBox = document.getElementById('status');
let selected = [];
let timer;

searchInput.addEventListener('input', () => {
  clearTimeout(timer);
  const q = searchInput.value.trim();
  if (!q) { results.style.display='none'; results.innerHTML=''; return; }
  timer = setTimeout(async () => {
    try {
      const res = await fetch('/api/companies/search?q=' + encodeURIComponent(q));
      const data = await res.json();
      results.innerHTML = '';
      const available = data.filter(c => !selected.some(s => s.name === c.name));
      if (!available.length) {
        results.innerHTML = '<div class="result">No matching companies found.</div>';
      } else {
        available.forEach(c => {
          const div = document.createElement('div');
          div.className='result';
          div.innerHTML = `<strong>${escapeHtml(c.name)}</strong><small>${escapeHtml(c.code || '—')} ${c.symbol ? '· ' + escapeHtml(c.symbol) : ''}</small>`;
          div.onclick = () => addCompany(c);
          results.appendChild(div);
        });
      }
      results.style.display='block';
    } catch(e) { results.innerHTML='<div class="result">Unable to search companies.</div>'; results.style.display='block'; }
  }, 250);
});

document.addEventListener('click', e => { if (!e.target.closest('.search-box') && !e.target.closest('.results')) results.style.display='none'; });

function addCompany(c) {
  if (selected.length >= 5) { statusBox.className='error'; statusBox.textContent='You can select up to 5 companies.'; return; }
  if (selected.some(s => s.name === c.name)) return;
  selected.push(c); renderSelected(); searchInput.value=''; results.style.display='none'; statusBox.textContent='';
}
function removeCompany(name) { selected = selected.filter(c => c.name !== name); renderSelected(); }
function renderSelected() {
  selectedBox.innerHTML='';
  selected.forEach(c => {
    const chip=document.createElement('div'); chip.className='chip';
    chip.innerHTML=`<span>${escapeHtml(c.name)}</span><button type="button" aria-label="Remove ${escapeHtml(c.name)}">×</button>`;
    chip.querySelector('button').onclick=()=>removeCompany(c.name); selectedBox.appendChild(chip);
  });
}
form.addEventListener('submit', async e => {
  e.preventDefault(); statusBox.className=''; statusBox.textContent='';
  if (!selected.length) { statusBox.className='error'; statusBox.textContent='Please select at least one company.'; return; }
  const button=form.querySelector('button'); button.disabled=true; button.textContent='Submitting...';
  const payload={name:document.getElementById('name').value.trim(),email:document.getElementById('email').value.trim(),phone:document.getElementById('phone').value.trim(),companies:selected.map(c=>c.name)};
  try {
    const res=await fetch('/api/stock-alert',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});
    const data=await res.json();
    if(!res.ok) throw new Error(data.message || 'Submission failed');
    statusBox.className='success'; statusBox.textContent='Successfully Submitted';
    form.reset(); selected=[]; renderSelected();
    setTimeout(()=>window.location.href='/',1800);
  } catch(err) { statusBox.className='error'; statusBox.textContent=err.message; button.disabled=false; button.textContent='Submit'; }
});
function escapeHtml(s){return String(s ?? '').replace(/[&<>'"]/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[m]));}

// Preselect a company passed from the home/header search, e.g. /stock-alert.html?company=ABC%20Ltd
(async function preselectFromUrl(){
  const company=new URLSearchParams(location.search).get('company');
  if(!company) return;
  try{
    const r=await fetch('/api/companies/search?q='+encodeURIComponent(company));
    const data=await r.json();
    const match=data.find(c=>String(c.name).toLowerCase()===company.toLowerCase()) || data[0];
    if(match && !selected.some(s=>s.name===match.name)){addCompany(match);}
  }catch(e){}
})();
