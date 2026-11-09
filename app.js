const $=(s,e=document)=>e.querySelector(s),$$=(s,e=document)=>[...e.querySelectorAll(s)];
const esc=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const fmt=n=>'₹'+Number(n||0).toLocaleString('en-IN',{maximumFractionDigits:2});
const sum=(a,f=x=>x.amount)=>a.reduce((s,x)=>s+f(x),0);
const S={user:null,tx:[],goals:[]},CH=[];
const COIN=`<svg class="coin" viewBox="0 0 64 64" aria-hidden="true"><defs><linearGradient id="cg" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#f6df96"/><stop offset=".5" stop-color="#d9b44a"/><stop offset="1" stop-color="#8f6f22"/></linearGradient></defs><circle cx="32" cy="32" r="30" fill="url(#cg)"/><circle cx="32" cy="32" r="25.5" fill="none" stroke="#052e1c" stroke-opacity=".5" stroke-width="1.5" stroke-dasharray="2 3"/><circle cx="32" cy="32" r="21" fill="#052e1c"/><text x="32" y="42.5" text-anchor="middle" font-family="Bricolage Grotesque,sans-serif" font-weight="700" font-size="28" fill="url(#cg)">$</text><path d="M11 24A24 24 0 0 1 28 8" stroke="#fff" stroke-opacity=".55" stroke-width="2" stroke-linecap="round" fill="none"/></svg>`;
const CAT={INCOME:['Salary','Freelance','Business','Investments','Gift','Other'],EXPENSE:['Food','Rent','Transport','Shopping','Bills','Health','Entertainment','Education','Other']};
const COL=['#1a8f4f','#d9b44a','#c8453d','#3b6ea5','#7a5ea8','#4aa3a2','#e08e45','#8a9a5b','#6b7c7a'];
const NAV=[['dashboard','Dashboard','◧'],['income','Income','↗'],['expenses','Expenses','↘'],['transactions','Transactions','⇄'],['savings','Savings','◎'],['budgets','Budgets','◔'],['analytics','Analytics','▤'],['profile','Profile','○']];
const go=h=>location.hash==='#/'+h?route():location.hash='#/'+h;
const toast=(m,bad)=>{const t=document.createElement('div');t.className='t'+(bad?' bad':'');t.textContent=m;$('#toast').append(t);setTimeout(()=>t.remove(),3200)};
const opts=(a,v)=>a.map(x=>`<option ${x===v?'selected':''}>${x}</option>`).join('');
const ym=d=>String(d).slice(0,7),ml=k=>new Date(k+'-01T00:00').toLocaleString('en',{month:'short'});
const months=n=>Array.from({length:n},(_,i)=>{const x=new Date(new Date().getFullYear(),new Date().getMonth()-(n-1-i),1);return x.getFullYear()+'-'+String(x.getMonth()+1).padStart(2,'0')});
const inc=l=>sum(l.filter(t=>t.type==='INCOME')),exp=l=>sum(l.filter(t=>t.type==='EXPENSE'));
const byCat=l=>l.reduce((o,x)=>(o[x.category]=(o[x.category]||0)+x.amount,o),{});
const empty=m=>`<div class="empty">${m}</div>`,pct=g=>Math.min(100,g.target?g.saved/g.target*100:0);
const gbar=g=>`<div class="gl"><div class="row sp"><b>${esc(g.name)}</b><span class="mut">${pct(g).toFixed(0)}%</span></div><div class="bar"><i style="width:${pct(g)}%"></i></div><small class="mut">${fmt(g.saved)} of ${fmt(g.target)}</small></div>`;

function modal(title,body,fn,btn='Save'){
  const m=document.createElement('div');m.className='modal';
  m.innerHTML=`<form class="card"><h3>${title}</h3>${body}<p class="err"></p><div class="row end"><button type="button" class="btn ghost" data-x>Cancel</button><button class="btn">${btn}</button></div></form>`;
  document.body.append(m);
  m.onclick=e=>{if(e.target===m||e.target.hasAttribute('data-x'))m.remove()};
  m.firstChild.onsubmit=async e=>{e.preventDefault();const b=$('.btn:not(.ghost)',m);b.disabled=true;
    try{await fn(Object.fromEntries(new FormData(e.target)));m.remove()}catch(x){$('.err',m).textContent=x.message;b.disabled=false}};
  return m;
}
function openTx(t={},type='EXPENSE'){
  type=t.type||type;
  const m=modal(t.id?'Edit transaction':'Add transaction',`
  <div class="seg">${['INCOME','EXPENSE'].map(v=>`<label><input type="radio" name="type" value="${v}" ${v===type?'checked':''}>${v==='INCOME'?'Income':'Expense'}</label>`).join('')}</div>
  <label>Amount<input name="amount" type="number" min="0.01" step="0.01" required value="${t.amount||''}"></label>
  <label>Category<select name="category">${opts(CAT[type],t.category)}</select></label>
  <label>Date<input name="date" type="date" required value="${t.date||new Date().toISOString().slice(0,10)}"></label>
  <label>Note<input name="note" maxlength="120" value="${esc(t.note)}"></label>`,
  async f=>{const b={...f,amount:+f.amount};await(t.id?api('PUT','/transactions/'+t.id,b):api('POST','/transactions',b));toast(t.id?'Transaction updated':'Transaction added');if(!t.id&&b.type==='INCOME')coinBurst();route()});
  $$('[name=type]',m).forEach(r=>r.onchange=()=>$('[name=category]',m).innerHTML=opts(CAT[r.value]));
}
function openGoal(g={}){
  modal(g.id?'Edit goal':'New savings goal',`
  <label>Goal name<input name="name" required maxlength="60" value="${esc(g.name)}"></label>
  <label>Target amount<input name="target" type="number" min="1" step="0.01" required value="${g.target||''}"></label>
  <label>Saved so far<input name="saved" type="number" min="0" step="0.01" value="${g.saved||0}"></label>
  <label>Deadline<input name="deadline" type="date" value="${g.deadline||''}"></label>`,
  async f=>{const b={...f,target:+f.target,saved:+f.saved||0};await(g.id?api('PUT','/goals/'+g.id,b):api('POST','/goals',b));toast('Goal saved');route()});
}
const table=l=>`<table><thead><tr><th>Date</th><th>Category</th><th>Note</th><th class="r">Amount</th><th></th></tr></thead><tbody>${l.map(t=>`<tr><td>${esc(t.date)}</td><td><span class="badge ${t.type}">${esc(t.category)}</span></td><td class="mut">${esc(t.note)||'—'}</td><td class="r ${t.type}">${t.type==='INCOME'?'+':'−'}${fmt(t.amount)}</td><td class="r"><button class="ic" data-edit="${t.id}" title="Edit" aria-label="Edit">✎</button><button class="ic" data-del="${t.id}" title="Delete" aria-label="Delete">✕</button></td></tr>`).join('')}</tbody></table>`;

function charts(n){
  if(!window.Chart)return;
  const ms=months(n),L=ms.map(ml),mt=k=>S.tx.filter(t=>ym(t.date)===k);
  const mk=(id,type,data,o={})=>CH.push(new Chart($('#'+id),{type,data,options:{responsive:true,maintainAspectRatio:false,plugins:{legend:{position:'bottom',labels:{usePointStyle:true}}},...o}}));
  mk('c1','bar',{labels:L,datasets:[{label:'Income',data:ms.map(k=>inc(mt(k))),backgroundColor:'#1a8f4f',borderRadius:4},{label:'Expenses',data:ms.map(k=>exp(mt(k))),backgroundColor:'#c8453d',borderRadius:4}]});
  const c=Object.entries(byCat(S.tx.filter(t=>t.type==='EXPENSE'&&ym(t.date)>=ms[0])));
  mk('c2','doughnut',{labels:c.map(x=>x[0]),datasets:[{data:c.map(x=>x[1]),backgroundColor:COL,borderWidth:0}]},{cutout:'66%'});
  let run=sum(S.tx.filter(t=>ym(t.date)<ms[0]),t=>t.type==='INCOME'?t.amount:-t.amount);
  mk('c3','line',{labels:L,datasets:[{label:'Cumulative net savings',data:ms.map(k=>run+=inc(mt(k))-exp(mt(k))),borderColor:'#d9b44a',backgroundColor:'rgba(217,180,74,.14)',fill:true,tension:.35,borderWidth:2}]});
}
function insights(){
  const [a,b]=months(2).map(k=>S.tx.filter(t=>ym(t.date)===k)),o=[];
  if(exp(a)>0){const c=(exp(b)-exp(a))/exp(a)*100;o.push(c>0?`Spending is up ${c.toFixed(0)}% on last month.`:`Spending is down ${(-c).toFixed(0)}% on last month.`)}
  if(inc(b)){const r=(inc(b)-exp(b))/inc(b)*100;o.push(r>=20?`You're saving ${r.toFixed(0)}% of this month's income. Keep that pace.`:`You're saving ${r.toFixed(0)}% of this month's income. Aim for 20% or more.`)}
  const c=Object.entries(byCat(b.filter(t=>t.type==='EXPENSE'))).sort((x,y)=>y[1]-x[1])[0];
  if(c)o.push(`${c[0]} is your biggest expense this month at ${fmt(c[1])}.`);
  (S.budgets||[]).forEach(x=>{const u=(byCat(b.filter(t=>t.type==='EXPENSE'))[x.category]||0)/x.limit*100;if(u>=80)o.push(`${x.category} has used ${u.toFixed(0)}% of its monthly budget.`)});
  return o.length?o:['Add income and expenses to see insights here.'];
}

function dashboard(){
  const T=S.tx,I=inc(T),E=exp(T),M=T.filter(t=>ym(t.date)===months(1)[0]),mi=inc(M),me=exp(M),r=T.filter(t=>t.date>=new Date(Date.now()-90*864e5).toISOString().slice(0,10)),dn=(inc(r)-exp(r))/90,nw=new Date(),left=Math.max(1,new Date(nw.getFullYear(),nw.getMonth()+1,0).getDate()-nw.getDate()),kv=(l,v,c='')=>`<div class="kv"><span>${l}</span><b class="${c}">${v}</b></div>`;
  $('#view').innerHTML=`<div class="head"><div><h2>${new Date().getHours()<12?'Good morning':new Date().getHours()<18?'Good afternoon':'Good evening'}, ${esc(S.user.name.split(' ')[0])}</h2><p class="mut">Here is where your money stands today.</p></div><div class="row"><button class="btn" data-add="INCOME">Add income</button><button class="btn ghost" data-add="EXPENSE">Add expense</button></div></div>
  <section class="card strip"><div><p class="mut">Total balance</p><h1 class="cu" data-v="${I-E}">${fmt(I-E)}</h1></div><div><p class="mut">Total income</p><h3 class="INCOME cu" data-v="${I}">${fmt(I)}</h3></div><div><p class="mut">Total expenses</p><h3 class="EXPENSE cu" data-v="${E}">${fmt(E)}</h3></div><div><p class="mut">Total savings</p><h3 class="cu" data-v="${sum(S.goals,g=>g.saved)}">${fmt(sum(S.goals,g=>g.saved))}</h3></div></section>
  <div class="cols"><div class="card"><h4>Income and expenses</h4><div class="ch"><canvas id="c1"></canvas></div></div><div class="card"><h4>Spending by category</h4><div class="ch"><canvas id="c2"></canvas></div></div></div>
  <div class="cols"><div class="card"><h4>Savings growth</h4><div class="ch"><canvas id="c3"></canvas></div></div>
  <div class="card"><h4>This month</h4>${kv('Income','+'+fmt(mi),'INCOME')}${kv('Expenses','−'+fmt(me),'EXPENSE')}${kv('Net',fmt(mi-me))}${kv('Savings rate',mi?((mi-me)/mi*100).toFixed(0)+'%':'—')}${kv('Safe to spend per day',fmt(Math.max(0,(mi-me)/left)))}${kv('Projected balance in 30 days',fmt(I-E+dn*30))}<h4 style="margin-top:20px">Insights</h4><ul class="ins">${insights().map(i=>`<li>${i}</li>`).join('')}</ul></div></div>
  <div class="cols"><div class="card tablewrap"><h4>Recent transactions</h4>${T.length?table([...T].sort((a,b)=>b.date.localeCompare(a.date)).slice(0,5)):empty('No transactions yet. Use Add income or Add expense to start.')}</div>
  <div class="card"><div class="row sp"><h4>Savings goals</h4><a href="#/savings">View all</a></div>${S.goals.length?S.goals.slice(0,3).map(gbar).join(''):empty('No goals yet. Create one on the Savings page.')}</div></div>`;
  charts(6);countUp();if(S.fx){S.fx=0;setTimeout(()=>coinBurst(),500)}
}
function ledger(type){
  const all=!type,title=all?'Transactions':type==='INCOME'?'Income':'Expenses';
  $('#view').innerHTML=`<div class="head"><div><h2>${title}</h2><p class="mut" id="sub"></p></div>${all?csvBtns:`<button class="btn" data-add="${type}">Add ${type==='INCOME'?'income':'expense'}</button>`}</div>
  <div class="card filters"><input id="q" placeholder="Search notes or categories">${all?'<select id="ft"><option value="">All types</option><option value="INCOME">Income</option><option value="EXPENSE">Expense</option></select>':''}<select id="fc"><option value="">All categories</option>${opts([...new Set([...CAT.INCOME,...CAT.EXPENSE])])}</select><input type="date" id="f1" aria-label="From date"><input type="date" id="f2" aria-label="To date"></div>
  <div class="card tablewrap" id="tb"></div>`;
  const draw=()=>{
    const q=$('#q').value.toLowerCase(),ft=type||($('#ft')||{}).value,fc=$('#fc').value,a=$('#f1').value,b=$('#f2').value;
    const l=S.tx.filter(t=>(!ft||t.type===ft)&&(!fc||t.category===fc)&&(!a||t.date>=a)&&(!b||t.date<=b)&&(!q||(t.note+' '+t.category).toLowerCase().includes(q))).sort((x,y)=>y.date.localeCompare(x.date));
    $('#sub').textContent=`${l.length} records, ${fmt(type?sum(l):sum(l,t=>t.type==='INCOME'?t.amount:-t.amount))} ${type?'total':'net'}`;
    $('#tb').innerHTML=l.length?table(l):empty('No records match. Change the filters or add a new one.');
  };
  $$('.filters input,.filters select').forEach(i=>i.oninput=draw);draw();const im=$('#imp');if(im)im.onchange=importCsv;
}
function savings(){
  const G=S.goals;
  $('#view').innerHTML=`<div class="head"><div><h2>Savings goals</h2><p class="mut">${fmt(sum(G,g=>g.saved))} saved of ${fmt(sum(G,g=>g.target))}</p></div><button class="btn" data-gadd>New goal</button></div>
  <div class="grid g3">${G.length?G.map(g=>{const d=g.deadline?Math.ceil((new Date(g.deadline)-Date.now())/864e5):null;
  return `<div class="card">${gbar(g)}<p class="mut sm" style="margin-bottom:12px">${d===null?'No deadline':d>=0?d+' days left':'Deadline passed'}</p><div class="row"><button class="btn sm" data-dep="${g.id}">Deposit</button><button class="ic" data-gedit="${g.id}" aria-label="Edit goal">✎</button><button class="ic" data-gdel="${g.id}" aria-label="Delete goal">✕</button></div></div>`}).join(''):empty('No goals yet. Select New goal to set a target.')}</div>`;
}
function analytics(n=6){
  const ms=months(n);
  $('#view').innerHTML=`<div class="head"><div><h2>Analytics</h2><p class="mut">Reports and month-by-month comparisons.</p></div><select id="pn" style="width:auto">${[3,6,12].map(x=>`<option value="${x}" ${x===n?'selected':''}>Last ${x} months</option>`).join('')}</select></div>
  <div class="cols"><div class="card"><h4>Income and expenses</h4><div class="ch"><canvas id="c1"></canvas></div></div><div class="card"><h4>Spending by category</h4><div class="ch"><canvas id="c2"></canvas></div></div></div>
  <div class="card"><h4>Savings growth</h4><div class="ch"><canvas id="c3"></canvas></div></div>
  <div class="card"><h4>Daily spending, last 18 weeks</h4>${heat()}</div>
  <div class="card"><h4>What if you spent less?</h4><div class="filters"><select id="wc" aria-label="Category">${opts(CAT.EXPENSE)}</select><input id="wp" type="range" min="5" max="50" step="5" value="20" aria-label="Reduction percent"></div><p id="wr" class="mut" style="margin-top:12px"></p></div>
  <div class="card tablewrap"><h4>Monthly comparison</h4><table><thead><tr><th>Month</th><th class="r">Income</th><th class="r">Expenses</th><th class="r">Net</th><th class="r">Savings rate</th></tr></thead><tbody>${[...ms].reverse().map(k=>{const l=S.tx.filter(t=>ym(t.date)===k),i=inc(l),e=exp(l);
  return `<tr><td>${ml(k)} ${k.slice(0,4)}</td><td class="r INCOME">${fmt(i)}</td><td class="r EXPENSE">${fmt(e)}</td><td class="r">${fmt(i-e)}</td><td class="r">${i?((i-e)/i*100).toFixed(0)+'%':'—'}</td></tr>`}).join('')}</tbody></table></div>`;
  $('#pn').onchange=e=>{CH.splice(0).forEach(c=>c.destroy());analytics(+e.target.value)};
  const wi=()=>{const c=$('#wc').value,p=+$('#wp').value,mm=months(3),a=sum(S.tx.filter(t=>t.type==='EXPENSE'&&t.category===c&&mm.includes(ym(t.date))))/3;$('#wr').innerHTML=a?`Cutting <b>${c}</b> by <b>${p}%</b> saves about <b>${fmt(a*p/100)}</b> a month and <b>${fmt(a*p*12/100)}</b> a year.`:`No ${c} spending in the last 3 months to simulate.`};
  $$('#wc,#wp').forEach(i=>i.oninput=wi);wi();
  charts(n);
}
function profile(){
  const u=S.user;
  $('#view').innerHTML=`<div class="head"><div><h2>Profile</h2><p class="mut">Manage your account details.</p></div></div>
  <form class="card narrow" id="pf"><label>Full name<input name="name" required maxlength="60" value="${esc(u.name)}"></label><label>Email<input name="email" type="email" required value="${esc(u.email)}"></label>
  <h4 style="margin-top:24px">Change password</h4><label>Current password<input name="currentPassword" type="password" autocomplete="current-password"></label><label>New password<input name="newPassword" type="password" minlength="8" autocomplete="new-password"></label>
  <p class="err"></p><button class="btn">Save changes</button></form>`;
  $('#pf').onsubmit=async e=>{e.preventDefault();const f=Object.fromEntries(new FormData(e.target)),er=$('.err');er.textContent='';
    if(!f.newPassword){delete f.newPassword;delete f.currentPassword}else if(!f.currentPassword)return er.textContent='Enter your current password to set a new one.';
    try{S.user=await api('PUT','/profile',f);toast('Profile updated');route()}catch(x){er.textContent=x.message}};
}
function authView(reg){
  $('#app').innerHTML=`<div class="auth">
    <section class="brand auth-visual" aria-label="LumaLedger introduction">
      <div class="auth-grain" aria-hidden="true"></div>
      <div class="auth-ambient" aria-hidden="true">
        <canvas class="auth-particle-canvas"></canvas>
        <div class="vault-emblem">
          <div class="vault-radial"></div>
          <div class="vault-ring vr-one"><i></i></div>
          <div class="vault-ring vr-two"><i></i></div>
          <div class="vault-ring vr-three"><i></i></div>
          <div class="vault-core"><span>₹</span><small>WEALTH ENGINE</small></div>
          <div class="vault-lens"></div>
        </div>
        <div class="ambient-chip chip-a"><i></i><b>SMART MONEY</b><span>BY DESIGN</span></div>
        <div class="ambient-chip chip-b"><span>✦</span><b>BUILD</b><b>YOUR FUTURE</b></div>
        <span class="ambient-stars"></span>
      </div>
      <div class="auth-brandline"><div class="logo big">${COIN}</div><span class="auth-wordmark">LumaLedger</span><span class="auth-brandtag">PERSONAL FINANCE, REFINED</span></div>
      <div class="auth-copy">
        <div class="auth-eyebrow"><i></i> YOUR MONEY. YOUR MOMENTUM.</div>
        <h1>Make every rupee<br><em>move you forward.</em></h1>
        <p>A clearer view of your money starts here. Track what comes in, understand what goes out, and build toward what matters.</p>
      </div>
      <div class="auth-preview" aria-label="Illustrative product preview">
        <div class="preview-head"><div><span class="preview-kicker">THE BIG PICTURE</span><strong>Your finances, in focus.</strong></div><span class="preview-live"><i></i> MADE FOR CLARITY</span></div>
        <div class="preview-chart-wrap">
          <div class="preview-chart-label"><span>Build better habits</span><b>↑</b></div>
          <svg class="preview-chart" viewBox="0 0 560 112" preserveAspectRatio="none" aria-hidden="true">
            <defs><linearGradient id="previewFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#d9b44a" stop-opacity=".3"/><stop offset="1" stop-color="#d9b44a" stop-opacity="0"/></linearGradient></defs>
            <path d="M0 91 C42 86 58 66 98 72 S157 89 198 57 S258 66 300 43 S355 60 398 29 S470 43 515 13 L560 7 L560 112 L0 112Z" fill="url(#previewFill)"/>
            <path d="M0 91 C42 86 58 66 98 72 S157 89 198 57 S258 66 300 43 S355 60 398 29 S470 43 515 13 L560 7" fill="none" stroke="#e3c36c" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="560" cy="7" r="4" fill="#fff1bf"/>
          </svg>
        </div>
        <div class="preview-features">
          <div><span class="preview-icon">↗</span><span><b>Track income</b><small>Know what you earn</small></span></div>
          <div><span class="preview-icon">◒</span><span><b>See spending</b><small>Spot patterns early</small></span></div>
          <div><span class="preview-icon">◎</span><span><b>Reach goals</b><small>Make progress visible</small></span></div>
        </div>
      </div>
      <div class="auth-visual-foot"><span>PLAN WITH PURPOSE</span><span class="auth-foot-line"></span><span>GROW WITH CONFIDENCE</span></div>
    </section>
    <section class="auth-side">
      <div class="auth-side-top"><span> <i></i> YOUR FINANCIAL SPACE</span><span>01 <b>/</b> 02</span></div>
      <form class="card auth-card" id="af" novalidate>
        <div class="auth-form-mark"><span class="auth-form-icon">${COIN}</span><span>${reg?'GET STARTED':'WELCOME BACK'}</span></div>
        <h2>${reg?'Create your account':'Good to see you.'}</h2>
        <p class="auth-form-sub">${reg?'A smarter relationship with money starts with one small step.':'Sign in to pick up right where your goals left off.'}</p>
        ${reg?'<label>Full name<input name="name" required maxlength="60" autocomplete="name" placeholder="Your full name"></label>':''}
        <label>Email address<input name="email" type="email" required autocomplete="email" placeholder="you@example.com"></label>
        <label>Password<input name="password" type="password" required ${reg?'minlength="8"':''} autocomplete="${reg?'new':'current'}-password" placeholder="${reg?'Create a strong password':'Enter your password'}"></label>
        ${reg?'<label>Confirm password<input name="confirm" type="password" required autocomplete="new-password" placeholder="Enter your password again"></label>':''}
        <p class="err" aria-live="polite"></p>
        <button class="btn full auth-submit"><span>${reg?'Create my account':'Sign in to LumaLedger'}</span><b aria-hidden="true">↗</b></button>
        <p class="auth-switch">${reg?'Already have an account? <a href="#/login">Sign in</a>':'New to LumaLedger? <a href="#/register">Create an account <span>↗</span></a>'}</p>
        <div class="auth-form-bottom"><span><i></i> PRIVATE BY DESIGN</span><span>BUILT FOR BETTER MONEY HABITS</span></div>
      </form>
      <div class="auth-side-foot">© ${new Date().getFullYear()} LumaLedger <span>·</span> Clarity looks good on you.</div>
    </section>
  </div>`;
  $('#af').onsubmit=async e=>{e.preventDefault();const f=Object.fromEntries(new FormData(e.target)),er=$('.err',e.target),b=$('.btn',e.target),t=b.innerHTML;
    if(reg&&f.password!==f.confirm)return er.textContent='Passwords do not match.';
    delete f.confirm;er.textContent='';b.disabled=true;b.innerHTML='<span>Just a moment…</span><b class="auth-button-spin" aria-hidden="true"></b>';
    try{S.user=await api('POST',reg?'/auth/register':'/auth/login',f);S.fx=1;go('dashboard')}catch(x){er.textContent=x.message;b.disabled=false;b.innerHTML=t}};
}
function shell(p){
  if(!$('.shell')){
    $('#app').innerHTML=`<div class="shell"><aside id="side"><div class="logo-row"><div class="logo">${COIN}</div>LumaLedger</div><nav>${NAV.map(n=>`<a href="#/${n[0]}" data-p="${n[0]}"><span>${n[2]}</span>${n[1]}</a>`).join('')}</nav>${API.MOCK?'<p class="demo">Demo mode: data is saved in this browser only.</p>':''}</aside>
    <div class="main"><header><button class="ic burger" id="bg" aria-label="Menu">☰</button><h3 id="ttl"></h3><div class="row"><button class="btn sm ghost" id="k">Search<kbd>Ctrl K</kbd></button><button class="ic" id="th" aria-label="Toggle theme" title="Toggle theme">◐</button><button class="btn sm" data-add="INCOME">Add income</button><button class="btn sm ghost" data-add="EXPENSE">Add expense</button><span class="av"></span><button class="btn sm ghost" id="lo">Log out</button></div></header><main id="view"></main></div></div>`;
    $('#bg').onclick=()=>$('#side').classList.toggle('open');
    $('#lo').onclick=async()=>{try{await api('POST','/auth/logout')}catch{}S.user=null;go('login')};
    $('.shell').onclick=act;$('#k').onclick=palette;$('#th').onclick=toggleTheme;
  }
  $('#ttl').textContent=(NAV.find(n=>n[0]===p)||NAV[0])[1];$('.av').textContent=S.user.name[0].toUpperCase();
  $$('nav a').forEach(a=>a.classList.toggle('on',a.dataset.p===p));
}
async function act(e){
  if(e.target.closest('nav a'))$('#side').classList.remove('open');
  const b=e.target.closest('[data-add],[data-edit],[data-del],[data-gadd],[data-gedit],[data-gdel],[data-dep],[data-badd],[data-bedit],[data-bdel],[data-csv]');if(!b)return;
  const d=b.dataset,tx=id=>S.tx.find(t=>t.id==id),gl=id=>S.goals.find(g=>g.id==id);
  const del=async(path,msg)=>{if(!confirm(msg))return;try{await api('DELETE',path);toast('Deleted');route()}catch(x){toast(x.message,1)}};
  if(d.add)openTx({},d.add);
  else if(d.edit)openTx(tx(d.edit));
  else if(d.del)del('/transactions/'+d.del,'Delete this transaction?');
  else if('gadd' in d)openGoal();
  else if(d.gedit)openGoal(gl(d.gedit));
  else if(d.gdel)del('/goals/'+d.gdel,'Delete this goal?');
  else if('csv' in d)exportCsv();
  else if('badd' in d)openBudget();
  else if(d.bedit)openBudget(S.budgets.find(x=>x.id==d.bedit));
  else if(d.bdel)del('/budgets/'+d.bdel,'Delete this budget?');
  else if(d.dep){const g=gl(d.dep);modal('Deposit to '+esc(g.name),'<label>Amount<input name="amount" type="number" min="0.01" step="0.01" required></label>',
    async f=>{await api('PUT','/goals/'+g.id,{name:g.name,target:g.target,deadline:g.deadline,saved:g.saved+ +f.amount});toast('Deposit added');coinBurst();route()},'Deposit')}
}
const R={dashboard,income:()=>ledger('INCOME'),expenses:()=>ledger('EXPENSE'),transactions:()=>ledger(),savings,budgets,analytics:()=>analytics(),profile};
async function route(){
  CH.splice(0).forEach(c=>c.destroy());
  const p=location.hash.slice(2)||'dashboard';
  if(!S.user){authView(p==='register');authFx();premiumAuthFx();return moneyNotesFx()}
  if(p==='login'||p==='register')return go('dashboard');
  shell(p);$('#view').innerHTML='<div class="spin"></div>';
  try{[S.tx,S.goals,S.budgets]=await Promise.all([api('GET','/transactions'),api('GET','/goals'),api('GET','/budgets')])}
  catch(e){if(e.status===401){S.user=null;return go('login')}toast(e.message,1);S.tx=[];S.goals=[];S.budgets=[]}
  (R[p]||dashboard)();
}
window.onhashchange=route;
(async()=>{applyTheme();try{S.user=await api('GET','/auth/me')}catch{}route()})();

function TH(){return localStorage.getItem('lumaledger_theme')||(matchMedia('(prefers-color-scheme:dark)').matches?'dark':'light')}
function applyTheme(){const d=TH()==='dark';document.documentElement.dataset.theme=TH();if(window.Chart){Chart.defaults.color=d?'#8db59b':'#4f6b5a';Chart.defaults.borderColor=d?'#16372a':'#d8e6db'}}
function toggleTheme(){localStorage.setItem('lumaledger_theme',TH()==='dark'?'light':'dark');applyTheme();route()}
function countUp(){if(matchMedia('(prefers-reduced-motion:reduce)').matches)return;$$('.cu').forEach(el=>{const v=+el.dataset.v,t0=performance.now(),f=t=>{const k=Math.min(1,(t-t0)/900);el.textContent=fmt(v*(1-Math.pow(1-k,3)));if(k<1)requestAnimationFrame(f)};requestAnimationFrame(f)})}
function palette(){
  const cmds=[...NAV.map(n=>['Go to '+n[1],()=>go(n[0])]),['Add income',()=>openTx({},'INCOME')],['Add expense',()=>openTx({},'EXPENSE')],['New savings goal',()=>openGoal()],['New budget',()=>openBudget()],['Export transactions as CSV',exportCsv],['Toggle theme',toggleTheme],['Log out',()=>$('#lo').click()]];
  const m=document.createElement('div');m.className='modal top';m.innerHTML='<div class="card pal"><input placeholder="Type a command…" aria-label="Command"><ul></ul></div>';document.body.append(m);
  const inp=$('input',m),ul=$('ul',m);let i=0,l=[];
  const draw=()=>{l=cmds.filter(c=>c[0].toLowerCase().includes(inp.value.toLowerCase()));i=Math.min(i,Math.max(l.length-1,0));ul.innerHTML=l.map((c,j)=>`<li class="${j===i?'on':''}" data-i="${j}">${c[0]}</li>`).join('')||'<li class="mut">No matching command</li>'};
  const run=j=>{m.remove();l[j]&&l[j][1]()};
  inp.oninput=()=>{i=0;draw()};
  inp.onkeydown=e=>{if(e.key==='ArrowDown'){i=Math.min(i+1,l.length-1);draw();e.preventDefault()}else if(e.key==='ArrowUp'){i=Math.max(i-1,0);draw();e.preventDefault()}else if(e.key==='Enter')run(i);else if(e.key==='Escape')m.remove()};
  ul.onclick=e=>{const li=e.target.closest('li[data-i]');if(li)run(+li.dataset.i)};
  m.onclick=e=>{if(e.target===m)m.remove()};inp.focus();draw();
}
document.addEventListener('keydown',e=>{if((e.ctrlKey||e.metaKey)&&e.key.toLowerCase()==='k'&&S.user){e.preventDefault();if(!$('.pal'))palette()}});

const csvBtns='<div class="row"><button class="btn ghost sm" data-csv>Export CSV</button><label class="btn ghost sm" style="margin:0;cursor:pointer">Import CSV<input type="file" accept=".csv" id="imp" hidden></label><button class="btn sm" data-add="EXPENSE">Add expense</button></div>';
function exportCsv(){const q=v=>'"'+String(v??'').replace(/"/g,'""')+'"',r=[['date','type','category','amount','note'],...S.tx.map(t=>[t.date,t.type,t.category,t.amount,t.note])],a=document.createElement('a');
  a.href=URL.createObjectURL(new Blob([r.map(x=>x.map(q).join(',')).join('\n')],{type:'text/csv'}));a.download='lumaledger-transactions.csv';a.click();toast('CSV exported')}
async function importCsv(e){
  const f=e.target.files[0];if(!f)return;let ok=0;
  const rows=(await f.text()).trim().split(/\r?\n/).slice(1).map(l=>(l.match(/("([^"]|"")*"|[^,]*)(,|$)/g)||[]).map(c=>c.replace(/,$/,'').replace(/^"|"$/g,'').replace(/""/g,'"')));
  for(const [date,type,category,amount,note] of rows){const ty=String(type).toUpperCase();if(!/^\d{4}-\d{2}-\d{2}$/.test(date)||!CAT[ty]||!(+amount>0))continue;try{await api('POST','/transactions',{date,type:ty,category,amount:+amount,note:note||''});ok++}catch{}}
  toast(ok?`Imported ${ok} transactions`:'No valid rows found. Use the columns: date, type, category, amount, note',!ok);route();
}
function openBudget(b={}){
  modal(b.id?'Edit budget':'New budget',`<label>Category<select name="category">${opts(CAT.EXPENSE,b.category)}</select></label><label>Monthly limit<input name="limit" type="number" min="1" step="0.01" required value="${b.limit||''}"></label>`,
  async f=>{if(!b.id&&S.budgets.some(x=>x.category===f.category))throw Error('A budget for that category already exists. Edit it instead.');const d={category:f.category,limit:+f.limit};await(b.id?api('PUT','/budgets/'+b.id,d):api('POST','/budgets',d));toast('Budget saved');route()});
}
function budgets(){
  const k=months(1)[0],sp=byCat(S.tx.filter(t=>t.type==='EXPENSE'&&ym(t.date)===k)),B=S.budgets;
  $('#view').innerHTML=`<div class="head"><div><h2>Budgets</h2><p class="mut">Monthly limits for ${new Date(k+'-01T00:00').toLocaleString('en',{month:'long',year:'numeric'})}.</p></div><button class="btn" data-badd>New budget</button></div>
  <div class="grid g3">${B.length?B.map(b=>{const s=sp[b.category]||0,u=s/b.limit*100,c=u>=100?'EXPENSE':u>=80?'warn':'ok';
  return `<div class="card"><div class="row sp"><b>${esc(b.category)}</b><span class="badge ${c}">${u>=100?'Over budget':u>=80?'Near limit':'On track'}</span></div><div class="bar ${c}"><i style="width:${Math.min(100,u)}%"></i></div><p class="mut sm">${fmt(s)} of ${fmt(b.limit)}, ${fmt(Math.max(0,b.limit-s))} left</p><div class="row" style="margin-top:10px"><button class="ic" data-bedit="${b.id}" aria-label="Edit budget">✎</button><button class="ic" data-bdel="${b.id}" aria-label="Delete budget">✕</button></div></div>`}).join(''):empty('No budgets yet. Select New budget to set a monthly limit for a category.')}</div>`;
}
function heat(){
  const m={};S.tx.filter(t=>t.type==='EXPENSE').forEach(t=>m[t.date]=(m[t.date]||0)+t.amount);
  const s=new Date();s.setDate(s.getDate()-125);const mx=Math.max(1,...Object.values(m)),c=[];
  for(let i=0;i<(s.getDay()+6)%7;i++)c.push('<i class="x"></i>');
  for(let i=0;i<126;i++){const d=new Date(s);d.setDate(s.getDate()+i);const k=d.toLocaleDateString('en-CA'),v=m[k]||0;c.push(`<i style="--a:${v?(.2+.8*v/mx).toFixed(2):0}" title="${k}: ${fmt(v)}"></i>`)}
  return `<div class="heat">${c.join('')}</div>`;
}

function authFx(){
  const f=$('#af');if(!f)return;
  $$('input[type=password]',f).forEach(i=>{
    const l=i.parentElement;
    l.insertAdjacentHTML('beforeend','<button type="button" class="eye" aria-label="Show password">Show</button><span class="caps" hidden>Caps Lock is on</span>');
    $('.eye',l).onclick=e=>{const sh=i.type==='password';i.type=sh?'text':'password';e.target.textContent=sh?'Hide':'Show';e.target.setAttribute('aria-label',sh?'Hide password':'Show password')};
    i.onkeyup=i.onkeydown=e=>{$('.caps',l).hidden=!(e.getModifierState&&e.getModifierState('CapsLock'))};
  });
  const pw=$('[name=password]',f);
  if($('[name=confirm]',f)&&pw){
    pw.parentElement.insertAdjacentHTML('afterend','<div class="meter"><i></i></div><small class="mut auth-strength" id="pt"></small>');
    pw.addEventListener('input',()=>{const v=pw.value,sc=Number(v.length>=8)+Number(v.length>=12)+Number(/[a-z]/.test(v)&&/[A-Z]/.test(v))+Number(/\d/.test(v))+Number(/[^\w\s]/.test(v)),m=$('.meter i',f);m.style.width=v?sc*20+'%':'0';m.dataset.s=sc;$('#pt',f).textContent=v?['Weak','Weak','Fair','Good','Strong','Strong'][sc]+' password':''});
  }
}
function moneyFx(){
  const b=$('.brand'),fl=$('.float');if(!b||!fl)return;
  fl.insertAdjacentHTML('beforeend',[10,38,62,86].map((x,i)=>`<b class="note" style="--x:${x}%;--d:${15+i*4}s;--l:-${i*5}s"></b>`).join(''));
  b.onmousemove=e=>{const r=b.getBoundingClientRect();fl.style.transform=`translate(${12-(e.clientX-r.left)/r.width*24}px,${12-(e.clientY-r.top)/r.height*24}px)`};
}
function coinBurst(x=innerWidth/2,y=innerHeight/3,n=30){
  if(matchMedia('(prefers-reduced-motion:reduce)').matches)return;
  for(let i=0;i<n;i++){const e=document.createElement('span'),s=14+Math.random()*14,a=Math.random()*6.28,v=120+Math.random()*260;
    e.className='cn';e.textContent='$';e.style.cssText=`left:${x}px;top:${y}px;width:${s}px;height:${s}px;font-size:${s*.55}px`;document.body.append(e);
    e.animate([{transform:'translate(0,0) rotateY(0)',opacity:1},{transform:`translate(${Math.cos(a)*v}px,${Math.sin(a)*v-120}px) rotateY(${360+Math.random()*360}deg)`,opacity:1,offset:.45},{transform:`translate(${Math.cos(a)*v*1.2}px,${Math.sin(a)*v+380}px) rotateY(900deg)`,opacity:0}],{duration:1400+Math.random()*600,easing:'cubic-bezier(.2,.7,.4,1)'}).onfinish=()=>e.remove();
  }
}
document.addEventListener('mousemove',e=>{
  const t=e.target.closest&&e.target.closest('.card');
  if(t){const r=t.getBoundingClientRect();t.style.setProperty('--mx',e.clientX-r.left+'px');t.style.setProperty('--my',e.clientY-r.top+'px')}
  const s=e.target.closest&&e.target.closest('.strip');
  if(s&&!matchMedia('(prefers-reduced-motion:reduce)').matches){const r=s.getBoundingClientRect(),x=(e.clientX-r.left)/r.width-.5,y=(e.clientY-r.top)/r.height-.5;s.style.transform=`perspective(900px) rotateY(${x*4}deg) rotateX(${-y*4}deg)`}
});
document.addEventListener('mouseout',e=>{const s=e.target.closest&&e.target.closest('.strip');if(s&&!s.contains(e.relatedTarget))s.style.transform=''});

function pageFx(){
  const a=$('.auth'),fl=$('.float'),b=$('.brand');if(!a||!fl)return;
  a.prepend(fl);b.onmousemove=null;
  fl.insertAdjacentHTML('beforeend',[14,30,46,60,76,92].map((x,i)=>`<span style="--x:${x}%;--s:${18+i%3*10}px;--d:${17+i*2}s;--l:-${i*2.5}s"></span>`).join(''));
  a.onmousemove=e=>{const r=a.getBoundingClientRect(),x=(e.clientX-r.left)/r.width,y=(e.clientY-r.top)/r.height;fl.style.transform=`translate(${12-x*24}px,${12-y*24}px)`};
}

function premiumAuthFx(){
  const visual=$('.auth-visual'),preview=$('.auth-preview'),form=$('.auth-card'),submit=$('.auth-submit'),auth=$('.auth');
  if(!visual||!auth)return;
  const reduced=matchMedia('(prefers-reduced-motion:reduce)').matches;
  requestAnimationFrame(()=>{
    visual.classList.add('motion-ready');
    if(form)form.classList.add('motion-ready');
    auth.classList.add('ultra-ready');
  });

  // Cursor-driven cinematic lighting and restrained depth.
  auth.addEventListener('pointermove',e=>{
    if(reduced)return;
    const r=auth.getBoundingClientRect(),x=(e.clientX-r.left)/r.width,y=(e.clientY-r.top)/r.height;
    auth.style.setProperty('--scene-x',(x*100).toFixed(1)+'%');
    auth.style.setProperty('--scene-y',(y*100).toFixed(1)+'%');
    const side=$('.auth-side');
    if(side&&e.target.closest('.auth-side')){
      const q=side.getBoundingClientRect();
      side.style.setProperty('--side-x',((e.clientX-q.left)/q.width*100).toFixed(1)+'%');
      side.style.setProperty('--side-y',((e.clientY-q.top)/q.height*100).toFixed(1)+'%');
    }
  },{passive:true});

  if(preview){
    const move=e=>{
      if(reduced)return;
      const r=preview.getBoundingClientRect(),x=(e.clientX-r.left)/r.width,y=(e.clientY-r.top)/r.height;
      preview.style.setProperty('--tilt-y',((x-.5)*9).toFixed(2)+'deg');
      preview.style.setProperty('--tilt-x',((.5-y)*7).toFixed(2)+'deg');
      preview.style.setProperty('--shine-x',(x*100).toFixed(1)+'%');
      preview.style.setProperty('--shine-y',(y*100).toFixed(1)+'%');
    };
    const reset=()=>{
      preview.style.setProperty('--tilt-y','0deg');preview.style.setProperty('--tilt-x','0deg');
      preview.style.setProperty('--shine-x','50%');preview.style.setProperty('--shine-y','50%');
    };
    preview.addEventListener('pointermove',move,{passive:true});
    preview.addEventListener('pointerleave',reset);
  }
  if(form){
    form.addEventListener('pointermove',e=>{
      if(reduced)return;
      const r=form.getBoundingClientRect(),x=(e.clientX-r.left)/r.width,y=(e.clientY-r.top)/r.height;
      form.style.setProperty('--card-x',(x*100).toFixed(1)+'%');
      form.style.setProperty('--card-y',(y*100).toFixed(1)+'%');
      form.style.setProperty('--card-tilt-x',((.5-y)*1.1).toFixed(2)+'deg');
      form.style.setProperty('--card-tilt-y',((x-.5)*1.1).toFixed(2)+'deg');
    },{passive:true});
    form.addEventListener('pointerleave',()=>{
      form.style.setProperty('--card-tilt-x','0deg');form.style.setProperty('--card-tilt-y','0deg');
    });
  }
  if(submit){
    submit.addEventListener('pointermove',e=>{
      const r=submit.getBoundingClientRect();
      submit.style.setProperty('--btn-x',(e.clientX-r.left)+'px');
      submit.style.setProperty('--btn-y',(e.clientY-r.top)+'px');
    },{passive:true});
  }
  const fields=form?$$('input',form):[];
  fields.forEach((field,i)=>{
    field.style.setProperty('--field-order',i);
    field.addEventListener('focus',()=>field.closest('label')?.classList.add('field-active'));
    field.addEventListener('blur',()=>field.closest('label')?.classList.remove('field-active'));
  });

  // Lightweight constellation canvas: subtle connected particles react to the pointer.
  const canvas=$('.auth-particle-canvas',visual),ctx=canvas&&canvas.getContext('2d');
  if(canvas&&ctx&&!reduced){
    let w=0,h=0,raf=0,alive=true,px=-1000,py=-1000,t=0;
    const dpr=Math.min(devicePixelRatio||1,1.6),dots=[];
    const resize=()=>{
      const r=visual.getBoundingClientRect();w=r.width;h=r.height;
      canvas.width=Math.round(w*dpr);canvas.height=Math.round(h*dpr);
      canvas.style.width=w+'px';canvas.style.height=h+'px';
      ctx.setTransform(dpr,0,0,dpr,0,0);
      const n=Math.max(42,Math.min(88,Math.round(w*h/10500)));
      while(dots.length<n)dots.push({x:Math.random()*w,y:Math.random()*h,vx:(Math.random()-.5)*.22,vy:(Math.random()-.5)*.22,r:.7+Math.random()*1.15,p:Math.random()*6.28});
      dots.length=n;
    };
    const pointer=e=>{const r=visual.getBoundingClientRect();px=e.clientX-r.left;py=e.clientY-r.top};
    const leave=()=>{px=py=-1000};
    resize();visual.addEventListener('pointermove',pointer,{passive:true});visual.addEventListener('pointerleave',leave);
    const frame=()=>{
      if(!alive||!canvas.isConnected){cancelAnimationFrame(raf);return}
      t+=.009;ctx.clearRect(0,0,w,h);
      for(let i=0;i<dots.length;i++){
        const p=dots[i];p.x+=p.vx+Math.sin(t+p.p)*.12;p.y+=p.vy+Math.cos(t*.8+p.p)*.12;
        if(p.x<0||p.x>w)p.vx*=-1;if(p.y<0||p.y>h)p.vy*=-1;
        const dx=p.x-px,dy=p.y-py,dist=Math.hypot(dx,dy);
        if(dist<115&&dist>0){const f=(115-dist)/115*.24;p.x+=dx/dist*f;p.y+=dy/dist*f}
        const alpha=.25+.28*(.5+.5*Math.sin(t*2+p.p));
        ctx.beginPath();ctx.arc(p.x,p.y,p.r,0,Math.PI*2);
        ctx.fillStyle=`rgba(224,196,125,${alpha})`;ctx.shadowBlur=9;ctx.shadowColor='#d7b45e';ctx.fill();ctx.shadowBlur=0;
        for(let k=i+1;k<dots.length;k++){
          const q=dots[k],dd=Math.hypot(p.x-q.x,p.y-q.y);
          if(dd<104){
            ctx.beginPath();ctx.moveTo(p.x,p.y);ctx.lineTo(q.x,q.y);
            ctx.strokeStyle=`rgba(174,211,180,${(1-dd/104)*.14})`;ctx.lineWidth=.65;ctx.stroke();
          }
        }
      }
      raf=requestAnimationFrame(frame);
    };
    frame();addEventListener('resize',resize);
    const observer=new MutationObserver(()=>{
      if(!visual.isConnected){alive=false;cancelAnimationFrame(raf);removeEventListener('resize',resize);visual.removeEventListener('pointermove',pointer);visual.removeEventListener('pointerleave',leave);observer.disconnect()}
    });
    observer.observe(document.body,{childList:true,subtree:true});
  }
}
function moneyNotesFx(){
  const a=$('.auth');
  if(!a||matchMedia('(prefers-reduced-motion: reduce)').matches)return;
  const layer=document.createElement('div');
  layer.className='cursor-money-layer';
  layer.setAttribute('aria-hidden','true');
  a.append(layer);
  let last=0;
  const spawn=e=>{
    const now=performance.now();
    if(now-last<35)return;
    last=now;
    const rect=a.getBoundingClientRect();
    const x=e.clientX-rect.left,y=e.clientY-rect.top;
    const note=document.createElement('span');
    note.className='cursor-money-note';
    note.textContent=['₹','₹','₹','₹','₹100','₹500'][Math.floor(Math.random()*6)];
    note.style.left=x+'px';note.style.top=y+'px';
    const dx=(Math.random()-.5)*85,dy=-35-Math.random()*65;
    note.style.setProperty('--dx',dx+'px');
    note.style.setProperty('--dy',dy+'px');
    note.style.setProperty('--rot',(Math.random()-.5)*65+'deg');
    layer.append(note);
    note.addEventListener('animationend',()=>note.remove(),{once:true});
  };
  a.addEventListener('pointermove',spawn,{passive:true});
  const observer=new MutationObserver(()=>{
    if(!layer.isConnected){a.removeEventListener('pointermove',spawn);observer.disconnect()}
  });
  observer.observe(document.body,{childList:true,subtree:true});
}
