// Set MOCK=false to talk to the Java backend (session cookie auth, JSON over /api/*).
const API={MOCK:true,BASE:'/api'};
async function api(m,p,b){
  if(API.MOCK)return mock(m,p,b);
  const r=await fetch(API.BASE+p,{method:m,credentials:'include',headers:{'Content-Type':'application/json'},body:b?JSON.stringify(b):undefined});
  const d=r.status===204?null:await r.json().catch(()=>({}));
  if(!r.ok)throw Object.assign(new Error((d&&d.message)||'Request failed ('+r.status+')'),{status:r.status});
  return d;
}
// Demo backend: data lives in localStorage. Passwords here are NOT secure; the real backend must use BCrypt.
const E=(s,t)=>Object.assign(new Error(t),{status:s}),pub=u=>({id:u.id,name:u.name,email:u.email});
async function mock(m,p,b){
  await new Promise(r=>setTimeout(r,250));
  const d=JSON.parse(localStorage.getItem('lumaledger')||'{"users":[],"tx":[],"goals":[],"seq":1}'),save=()=>localStorage.setItem('lumaledger',JSON.stringify(d));
  const uid=+localStorage.getItem('lumaledger_uid')||0,me=d.users.find(u=>u.id===uid);
  if(p==='/auth/register'){
    if(d.users.some(u=>u.email===b.email))throw E(409,'That email is already registered. Log in instead.');
    const u={id:d.seq++,name:b.name,email:b.email,pw:btoa(b.password)};d.users.push(u);save();localStorage.setItem('lumaledger_uid',u.id);return pub(u);
  }
  if(p==='/auth/login'){
    const u=d.users.find(u=>u.email===b.email&&u.pw===btoa(b.password));
    if(!u)throw E(401,'Email or password is incorrect.');
    localStorage.setItem('lumaledger_uid',u.id);return pub(u);
  }
  if(p==='/auth/logout'){localStorage.removeItem('lumaledger_uid');return null}
  if(!me)throw E(401,'Please log in to continue.');
  if(p==='/auth/me')return pub(me);
  const [,res,id]=p.split('/');
  if(res==='profile'){
    if(b.newPassword){if(btoa(b.currentPassword||'')!==me.pw)throw E(400,'Current password is incorrect.');me.pw=btoa(b.newPassword)}
    me.name=b.name;me.email=b.email;save();return pub(me);
  }
  d.budgets=d.budgets||[];const list=d[{transactions:'tx',goals:'goals',budgets:'budgets'}[res]];
  if(m==='GET')return list.filter(x=>x.uid===uid);
  if(m==='POST'){const x={...b,id:d.seq++,uid};list.push(x);save();return x}
  const i=list.findIndex(x=>x.id==id&&x.uid===uid);
  if(i<0)throw E(404,'Record not found.');
  if(m==='PUT'){list[i]={...list[i],...b,id:list[i].id,uid};save();return list[i]}
  list.splice(i,1);save();return null;
}
