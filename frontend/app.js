// TenantForge front-end console (vanilla JS, no build)
// - Unified API client with 401 auto-refresh
// - Sectioned UI for Auth/Projects/Tasks/TimeEntries/Reports/Health
// - Token decode and helpful UX touches

(function(){
  const $ = (s) => document.querySelector(s);
  const $$ = (s) => Array.from(document.querySelectorAll(s));

  const defaultBase = (() => {
    try{
      const p = new URLSearchParams(location.search);
      const q = p.get('base') || p.get('backend') || p.get('url');
      if(q) return q;
    }catch(e){}
    const meta = document.querySelector('meta[name="backend-base-url"]');
    if(meta && meta.content) return meta.content;
    if(typeof window !== 'undefined' && window.BACKEND_BASE_URL) return window.BACKEND_BASE_URL;
    return 'http://localhost:8080';
  })();

  const state = {
    get base(){ return localStorage.getItem('backendBaseUrl') || defaultBase; },
    set base(v){ localStorage.setItem('backendBaseUrl', v); renderBase(); },
    get access(){ return localStorage.getItem('accessToken') || ''; },
    set access(v){ v? localStorage.setItem('accessToken', v) : localStorage.removeItem('accessToken'); renderTokenInfo(); },
    get refresh(){ return localStorage.getItem('refreshToken') || ''; },
    set refresh(v){ v? localStorage.setItem('refreshToken', v) : localStorage.removeItem('refreshToken'); renderTokenInfo(); },
  };

  function normalizeBaseStrict(v){
    if(!v) return '';
    let s = String(v).trim();
    // Replace common unicode dashes with ASCII hyphen and strip spaces
    s = s.replace(/[\u2012\u2013\u2014\u2212]/g, '-').replace(/\s+/g,'');
    if(!/^https?:\/\//i.test(s)) s = 'https://' + s;
    s = s.replace(/\/+$/,'');
    return s;
  }

  function toast(msg, kind='info'){ const t=$('#toast'); t.textContent=msg; t.classList.add('show'); setTimeout(()=>t.classList.remove('show'), 2200); }

  function b64urlToUtf8(s){ try{ s=s.replace(/-/g,'+').replace(/_/g,'/'); const pad=s.length%4?4-(s.length%4):0; return decodeURIComponent(escape(atob(s+'='.repeat(pad)))); }catch(e){ return ''; }}
  function decodeJwt(tok){ if(!tok||tok.split('.').length<2) return null; try{ return JSON.parse(b64urlToUtf8(tok.split('.')[1])); }catch(e){ return null; } }
  function userIdFromAccess(){ const c=decodeJwt(state.access)||{}; return c.sub||''; }

  async function api(path, {method='GET', body=null, headers={}, retry=true, noAuth=false}={}){
    const h = {'Content-Type':'application/json', ...headers};
    if(!noAuth && state.access) h['Authorization'] = 'Bearer '+state.access;
    const base = normalizeBaseStrict(state.base);
    const resp = await fetch(base+path, {method, headers:h, body: body?JSON.stringify(body):null});
    const text = await resp.text(); let data; try{ data = JSON.parse(text);}catch(e){ data = text; }
    if(resp.status===401 && retry && state.refresh && !path.startsWith('/api/auth/refresh')){
      const ok = await doRefresh();
      if(ok) return api(path, {method, body, headers, retry:false, noAuth});
    }
    return {ok: resp.ok, status: resp.status, data};
  }

  function requireAuth(){
    if(!state.access){ toast('请先在“认证”页登录/注册获取令牌','err'); return false; }
    return true;
  }
  function requireUuid(id){
    if(!isUuid(id||'')){ toast('请先选择表格中的一行，或输入有效的 UUID','err'); return false; }
    return true;
  }

  async function doRefresh(){
    if(!state.refresh){ toast('无刷新令牌','warn'); return false; }
    const r = await api('/api/auth/refresh', {method:'POST', body:{refreshToken: state.refresh}, noAuth:true});
    if(r.ok){
      state.access = r.data.accessToken; state.refresh = r.data.refreshToken; toast('已刷新令牌','ok'); return true;
    }
    toast('刷新失败：'+(r.status||'') ,'err'); return false;
  }

  function renderBase(){ $('#baseNow').textContent = state.base; }
  function renderTokenInfo(){
    const a = state.access; const c = decodeJwt(a)||{}; const rc = decodeJwt(state.refresh)||{};
    const parts=[];
    if(a) parts.push('sub='+(c.sub||''));
    if(c.tenant_id) parts.push('tenant='+c.tenant_id);
    if(c.exp) parts.push('exp='+new Date(c.exp*1000).toISOString());
    if(state.refresh && rc.exp) parts.push('rExp='+new Date(rc.exp*1000).toISOString());
    $('#tokenInfo').textContent = parts.join(' | ');
    $('#userIdDefault').textContent = userIdFromAccess()||'-';
  }

  function tableFrom(items, columns){
    if(!Array.isArray(items)) items = [items];
    const tbl = document.createElement('table');
    const thead = document.createElement('thead'); const trh = document.createElement('tr');
    columns.forEach(c=>{ const th=document.createElement('th'); th.textContent=c.label; trh.appendChild(th); });
    thead.appendChild(trh); tbl.appendChild(thead);
    const tbody=document.createElement('tbody');
    items.forEach(it=>{
      const tr=document.createElement('tr');
      columns.forEach(c=>{ const td=document.createElement('td'); td.textContent = safe(get(it, c.key)); tr.appendChild(td); });
      tbody.appendChild(tr);
    });
    tbl.appendChild(tbody); return tbl;
  }
  function isUuid(s){ return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(String(s).trim()); }
  function get(o, path){ try{ return path.split('.').reduce((x,k)=>(x||{})[k], o); }catch(e){ return undefined; } }
  function safe(v){ if(v==null) return ''; if(typeof v==='object') return JSON.stringify(v); return String(v); }
  function setActive(section){
    $$('#nav a').forEach(a=>a.classList.toggle('active', a.dataset.target===section));
    ['auth','projects','tasks','time','reports','health'].forEach(id=>{
      const el = '#'+id; const node=$(el); if(node) node.hidden = (id!==section);
    });
  }

  // Event binding
  function bind(){
    // nav
    $$('#nav a').forEach(a=> a.addEventListener('click', (e)=>{ e.preventDefault(); setActive(a.dataset.target); history.replaceState(null,'','#'+a.dataset.target); }));
    // base
    $('#btnSaveBase').addEventListener('click', ()=>{ const v=normalizeBaseStrict($('#base').value); state.base=v; toast('已保存后端地址'); $('#linkOpenApi').href=state.base+'/v3/api-docs'; });

    // auth
    $('#registerBtn').addEventListener('click', async ()=>{
      const btn = $('#registerBtn'); btn.disabled=true; const body={
        tenantName: $('#tenantName').value, email: $('#email').value, password: $('#password').value, displayName: $('#displayName').value
      };
      const r = await api('/api/auth/register',{method:'POST', body});
      if(r.ok){ state.access=r.data.accessToken; state.refresh=r.data.refreshToken; $('#registerOut').className='ok'; $('#registerOut').textContent='✅ 注册成功'; toast('注册成功'); }
      else { $('#registerOut').className='err'; $('#registerOut').textContent=fmt(r); }
      btn.disabled=false;
    });
    $('#loginBtn').addEventListener('click', async ()=>{
      const btn=$('#loginBtn'); btn.disabled=true; const body={ email: $('#email').value, password: $('#password').value };
      const r = await api('/api/auth/login',{method:'POST', body});
      if(r.ok){ state.access=r.data.accessToken; state.refresh=r.data.refreshToken; $('#loginOut').className='ok'; $('#loginOut').textContent='✅ 登录成功'; toast('登录成功'); }
      else { $('#loginOut').className='err'; $('#loginOut').textContent=fmt(r); }
      btn.disabled=false;
    });
    $('#btnRefresh').addEventListener('click', doRefresh);
    $('#btnLogout').addEventListener('click', ()=>{ state.access=''; state.refresh=''; toast('已退出'); });
    $('#btnCopyAccess').addEventListener('click', async ()=>{ if(!state.access){ toast('无 Access Token'); return; } await navigator.clipboard.writeText(state.access); toast('已复制 Access'); });

    // projects
    $('#projList').addEventListener('click', async ()=>{ const q = new URLSearchParams({ q: $('#projQ').value, page:'0', size:'20', sort:'createdAt', order:'desc' }); const r = await api('/api/projects?'+q.toString()); renderList(r, '#projTableWrap', ['id','name','description','createdAt'], (row)=>{ $('#projId').value=row.id; $('#projName').value=row.name; $('#projDesc').value=row.description||''; }); });
    $('#projCreate').addEventListener('click', async ()=>{ if(!requireAuth()) return; const r = await api('/api/projects',{method:'POST', body:{ name: $('#projName').value, description: $('#projDesc').value }}); renderOut(r,'#projectsOut'); if(r.ok) toast('项目已创建'); });
    $('#projGet').addEventListener('click', async ()=>{
      const raw=$('#projId').value.trim();
      if(isUuid(raw)){
        const r=await api('/api/projects/'+raw); renderOut(r,'#projectsOut'); return;
      }
      // 容错：非UUID时按名称模糊查询
      const q = new URLSearchParams({ q: raw, page:'0', size:'10' });
      const r = await api('/api/projects?'+q.toString());
      if(r.ok){ renderList(r,'#projTableWrap',['id','name','description','createdAt'], (row)=>{ $('#projId').value=row.id; $('#projName').value=row.name; $('#projDesc').value=row.description||''; }); toast('已按名称查询，点击行可回填ID'); }
      else { renderOut(r,'#projectsOut'); }
    });
    $('#projUpdate').addEventListener('click', async ()=>{ if(!requireAuth()) return; const id=$('#projId').value.trim(); if(!requireUuid(id)) return; const r=await api('/api/projects/'+id,{method:'PUT', body:{name:$('#projName').value, description:$('#projDesc').value}}); renderOut(r,'#projectsOut'); if(r.ok) toast('项目已更新'); });
    $('#projDelete').addEventListener('click', async ()=>{ if(!requireAuth()) return; const id=$('#projId').value.trim(); if(!requireUuid(id)) return; const r=await api('/api/projects/'+id,{method:'DELETE'}); renderOut(r,'#projectsOut'); if(r.ok) toast('项目已删除'); });

    // tasks
    $('#taskList').addEventListener('click', async ()=>{ const p=new URLSearchParams({ q:$('#taskQ').value, projectId:$('#taskProjectId').value.trim(), status:$('#taskStatus').value, page:'0', size:'20', sort:'createdAt', order:'desc' }); const r=await api('/api/tasks?'+p.toString()); renderList(r,'#taskTableWrap',['id','projectId','name','status','createdAt'], (row)=>{ $('#taskId').value=row.id; $('#taskName').value=row.name; $('#taskStatus').value=row.status||''; $('#taskProjectId').value=row.projectId||''; }); });
    $('#taskCreate').addEventListener('click', async ()=>{ if(!requireAuth()) return; const r=await api('/api/tasks',{method:'POST', body:{ projectId:$('#taskProjectId').value.trim(), name:$('#taskName').value }}); renderOut(r,'#tasksOut'); if(r.ok) toast('任务已创建'); });
    $('#taskGet').addEventListener('click', async ()=>{ const id=$('#taskId').value.trim(); const r=await api('/api/tasks/'+id); renderOut(r,'#tasksOut'); });
    $('#taskUpdate').addEventListener('click', async ()=>{ if(!requireAuth()) return; const id=$('#taskId').value.trim(); if(!requireUuid(id)) return; const r=await api('/api/tasks/'+id,{method:'PUT', body:{ name:$('#taskName').value, status:$('#taskStatus').value||'OPEN'}}); renderOut(r,'#tasksOut'); if(r.ok) toast('任务已更新'); });
    $('#taskDelete').addEventListener('click', async ()=>{ if(!requireAuth()) return; const id=$('#taskId').value.trim(); if(!requireUuid(id)) return; const r=await api('/api/tasks/'+id,{method:'DELETE'}); renderOut(r,'#tasksOut'); if(r.ok) toast('任务已删除'); });

    // time entries
    $('#teCreate').addEventListener('click', async ()=>{ if(!requireAuth()) return; const body={ taskId:$('#teTaskId').value.trim(), userId:($('#teUserId').value.trim()||userIdFromAccess()), startedAt:($('#teStart').value||isoMinusHours(1)), endedAt:($('#teEnd').value||isoNow()), notes:$('#teNotes').value }; const r=await api('/api/time-entries',{method:'POST', body}); renderOut(r,'#teOut'); if(r.ok) toast('工时已创建'); });
    $('#teGet').addEventListener('click', async ()=>{ const id=$('#teId').value.trim(); const r=await api('/api/time-entries/'+id); renderOut(r,'#teOut'); });
    $('#teUpdate').addEventListener('click', async ()=>{ if(!requireAuth()) return; const id=$('#teId').value.trim(); if(!requireUuid(id)) return; const body={ startedAt:($('#teStart').value||isoMinusHours(1)), endedAt:($('#teEnd').value||isoNow()), notes:$('#teNotes').value }; const r=await api('/api/time-entries/'+id,{method:'PUT', body}); renderOut(r,'#teOut'); if(r.ok) toast('工时已更新'); });
    $('#teDelete').addEventListener('click', async ()=>{ if(!requireAuth()) return; const id=$('#teId').value.trim(); if(!requireUuid(id)) return; const r=await api('/api/time-entries/'+id,{method:'DELETE'}); renderOut(r,'#teOut'); if(r.ok) toast('工时已删除'); });
    $('#teList').addEventListener('click', async ()=>{ const p=new URLSearchParams({ start:$('#teFilterStart').value, end:$('#teFilterEnd').value, taskId:$('#teTaskId').value.trim(), userId:$('#teUserId').value.trim(), page:'0', size:'20', sort:'startedAt', order:'desc' }); const r=await api('/api/time-entries?'+p.toString()); renderList(r,'#teTableWrap',['id','taskId','userId','startedAt','endedAt','notes'], (row)=>{ $('#teId').value=row.id; $('#teTaskId').value=row.taskId||''; $('#teUserId').value=row.userId||''; $('#teStart').value=row.startedAt||''; $('#teEnd').value=row.endedAt||''; $('#teNotes').value=row.notes||''; }); });

    // reports
    $('#rJson').addEventListener('click', async ()=>{ const p=getReportParams(); const r=await api('/api/reports/time?'+p.toString()); renderOut(r,'#reportOut'); });
    $('#rCsv').addEventListener('click', async ()=>{ const p=getReportParams(); p.set('format','csv'); const u=normalizeBaseStrict(state.base)+'/api/reports/time?'+p.toString(); window.open(u,'_blank'); });

    // health
    $('#btnHealth').addEventListener('click', async ()=>{ const r=await api('/api/health'); renderOut(r,'#healthOut'); if(r.ok) toast('健康：OK'); });
  }

  function renderList(result, wrapSel, columns, onRowClick){
    const wrap = typeof wrapSel==='string' ? document.querySelector(wrapSel) : wrapSel;
    wrap.innerHTML='';
    if(!result.ok){ wrap.innerHTML = '<span class="err">'+fmt(result)+'</span>'; return; }
    const data = Array.isArray(result.data?.content) ? result.data.content : (Array.isArray(result.data) ? result.data : [result.data]);
    const cols = columns.map(k=>({key:k,label:k}));
    const tbl = tableFrom(data, cols);
    tbl.querySelectorAll('tbody tr').forEach((tr,idx)=>{
      tr.style.cursor='pointer';
      tr.addEventListener('click', ()=>{ onRowClick && onRowClick(data[idx]); toast('已选择一行'); });
    });
    wrap.appendChild(tbl);
  }

  function renderOut(r, sel){ const el=$(sel); el.className = r.ok? 'ok note':'err note'; el.textContent = fmt(r); }
  function fmt(r){ const body = typeof r.data==='string' ? r.data : JSON.stringify(r.data,null,2); return 'HTTP '+r.status+'\n'+body; }
  function isoNow(){ return new Date().toISOString(); }
  function isoMinusHours(h){ return new Date(Date.now()-h*3600*1000).toISOString(); }

  function init(){
    // Persist default base if empty and default is non-local
    if(!localStorage.getItem('backendBaseUrl') && defaultBase && defaultBase!=='http://localhost:8080'){
      localStorage.setItem('backendBaseUrl', defaultBase);
    }
    // Normalize any previously saved invalid base (missing scheme, unicode dash, etc.)
    const normalized = normalizeBaseStrict(state.base);
    if(normalized !== state.base){ state.base = normalized; }
    $('#base').value = state.base;
    renderBase(); renderTokenInfo(); bind();
    $('#linkOpenApi').href = state.base + '/v3/api-docs';
    // deep link section
    const hash = (location.hash||'').replace('#',''); if(hash) setActive(hash); else setActive('auth');
  }

  document.addEventListener('DOMContentLoaded', init);
})();
