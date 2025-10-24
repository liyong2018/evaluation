// Simple end-to-end verification for algorithm management update flow
// - Fetch algorithm list
// - Fetch detail for the first algorithm
// - Send update request with identical payload
// - Print results

(async () => {
  const base = 'http://localhost:8083/api/algorithm/management'
  const headers = { 'Content-Type': 'application/json' }

  const safeJson = async (res) => {
    const text = await res.text()
    try { return JSON.parse(text) } catch { return { raw: text } }
  }

  try {
    const listRes = await fetch(`${base}/list`)
    console.log('list status', listRes.status)
    const list = await safeJson(listRes)
    if (!list || !Array.isArray(list.data) || list.data.length === 0) {
      console.log('no algorithms or invalid list response:', list)
      return
    }

    const alg = list.data[0]
    console.log('use algorithm id', alg.id, 'name', alg.configName)

    const detailRes = await fetch(`${base}/detail/${alg.id}`)
    console.log('detail status', detailRes.status)
    const detail = await safeJson(detailRes)
    if (!detail || detail.success === false) {
      console.log('detail failed', detail && detail.message)
      return
    }

    const d = detail.data
    const payload = {
      id: d.algorithm.id,
      configName: d.algorithm.configName,
      description: d.algorithm.description,
      version: d.algorithm.version,
      status: d.algorithm.status,
      steps: (d.steps || []).map((s) => ({
        ...s,
        // Ensure formulaIds is a comma string as backend expects
        formulaIds: typeof s.formulaIds === 'string'
          ? s.formulaIds
          : (Array.isArray(s.formulaIds) ? s.formulaIds.join(',') : '')
      })),
      formulas: d.formulas || []
    }

    const upRes = await fetch(`${base}/update`, { method: 'PUT', headers, body: JSON.stringify(payload) })
    console.log('update status', upRes.status)
    const up = await safeJson(upRes)
    console.log('update response', up)
  } catch (e) {
    console.error('verify_update error', e)
  }
})()