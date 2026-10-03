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
function refreshAccount(){const u=currentUser();if(u){accountStatus.textContent='Logged in as '+u.name;loginOpen.classList.add('hidden');logoutBtn.classList.remove('hidden');accountButton.title=u.email;}else{accountStatus.textContent='Not logged in';loginOpen.classList.remove('hidden');logoutBtn.classList.add('hidden');accountButton.title='Login';}}
function toggleAccountMenu(){accountMenu.classList.toggle('open')}
function closeAccountMenu(){accountMenu.classList.remove('open')}
accountButton.onclick=toggleAccountMenu;
loginOpen.onclick=()=>{closeAccountMenu();loginError.textContent='';loginModal.classList.remove('hidden');document.getElementById('loginName').focus()};
loginClose.onclick=()=>loginModal.classList.add('hidden');
loginModal.onclick=e=>{if(e.target===loginModal)loginModal.classList.add('hidden')};
loginBtn.onclick=()=>{const name=document.getElementById('loginName').value.trim();const email=document.getElementById('loginEmail').value.trim();if(!name||!email||!email.includes('@')){loginError.textContent='Please enter a valid name and email.';return}localStorage.setItem('rikUser',JSON.stringify({name,email}));loginModal.classList.add('hidden');refreshAccount()};
logoutBtn.onclick=()=>{localStorage.removeItem('rikUser');refreshAccount();closeAccountMenu()};
document.addEventListener('click',e=>{if(!e.target.closest('.account-wrap'))closeAccountMenu()});
refreshAccount();
