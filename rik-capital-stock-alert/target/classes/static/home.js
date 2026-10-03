const homeSearch=document.getElementById('homeSearch');
const homeResults=document.getElementById('homeResults');
let homeTimer;

homeSearch.addEventListener('input',()=>{
  clearTimeout(homeTimer);
  const q=homeSearch.value.trim();
  if(!q){homeResults.innerHTML='';homeResults.style.display='none';return;}
  homeTimer=setTimeout(async()=>{
    try{
      const res=await fetch('/api/companies/search?q='+encodeURIComponent(q));
      const data=await res.json();
      homeResults.innerHTML='';
      if(!data.length) homeResults.innerHTML='<div class="home-result empty">No matching companies found.</div>';
      data.slice(0,10).forEach(c=>{
        const row=document.createElement('div');row.className='home-result';
        row.innerHTML=`<strong>${escapeHtml(c.name)}</strong><small>${escapeHtml(c.code||'—')} ${c.symbol?'· '+escapeHtml(c.symbol):''}</small>`;
        row.onclick=()=>window.location.href='/stock-alert.html?company='+encodeURIComponent(c.name);
        homeResults.appendChild(row);
      });
      homeResults.style.display='block';
    }catch(e){homeResults.innerHTML='<div class="home-result empty">Unable to search companies.</div>';homeResults.style.display='block';}
  },250);
});

document.addEventListener('click',e=>{
  if(!e.target.closest('.home-search-wrap')) homeResults.style.display='none';
  if(!e.target.closest('.account-wrap')) closeAccountMenu();
});

function clearHomeSearch(){homeSearch.value='';homeResults.innerHTML='';homeResults.style.display='none';homeSearch.focus();}
function escapeHtml(s){return String(s??'').replace(/[&<>'"]/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[m]));}

const accountButton=document.getElementById('accountButton');
const accountMenu=document.getElementById('accountMenu');
const accountStatus=document.getElementById('accountStatus');
const loginOpen=document.getElementById('loginOpen');
const logoutBtn=document.getElementById('logoutBtn');
const loginModal=document.getElementById('loginModal');
const loginClose=document.getElementById('loginClose');
const loginBtn=document.getElementById('loginBtn');
const loginError=document.getElementById('loginError');

function currentUser(){try{return JSON.parse(localStorage.getItem('rikUser')||'null')}catch{return null}}
function refreshAccount(){
  const u=currentUser();
  if(u){accountStatus.textContent='Logged in as '+u.name;loginOpen.classList.add('hidden');logoutBtn.classList.remove('hidden');accountButton.title=u.email;}
  else{accountStatus.textContent='Not logged in';loginOpen.classList.remove('hidden');logoutBtn.classList.add('hidden');accountButton.title='Login';}
}
function toggleAccountMenu(){accountMenu.classList.toggle('open');}
function closeAccountMenu(){accountMenu.classList.remove('open');}
accountButton.onclick=toggleAccountMenu;
loginOpen.onclick=()=>{closeAccountMenu();loginError.textContent='';loginModal.classList.remove('hidden');document.getElementById('loginName').focus();};
loginClose.onclick=()=>loginModal.classList.add('hidden');
loginModal.onclick=e=>{if(e.target===loginModal)loginModal.classList.add('hidden');};
loginBtn.onclick=()=>{
 const name=document.getElementById('loginName').value.trim(),email=document.getElementById('loginEmail').value.trim();
 if(!name||!email||!email.includes('@')){loginError.textContent='Please enter a valid name and email.';return;}
 localStorage.setItem('rikUser',JSON.stringify({name,email}));loginModal.classList.add('hidden');refreshAccount();
};
logoutBtn.onclick=()=>{localStorage.removeItem('rikUser');refreshAccount();closeAccountMenu();};
refreshAccount();
