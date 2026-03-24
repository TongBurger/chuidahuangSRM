/**
 * 数据导出工具函数
 */

export interface ExportColumn {
  key: string
  label: string
  formatter?: (value: any, row: any) => string
}

export interface ExportOptions {
  title?: string
  filename?: string
  columns: ExportColumn[]
  data: any[]
}

/**
 * 导出数据为 CSV 文件
 */
export function exportToCSV(options: ExportOptions): void {
  const { title = '数据导出', filename = 'export', columns, data } = options

  // CSV BOM for UTF-8 (支持中文)
  let csvContent = '\uFEFF'

  // 添加标题行
  csvContent += columns.map(col => col.label).join(',') + '\n'

  // 添加数据行
  data.forEach(row => {
    const values = columns.map(col => {
      let value = row[col.key]
      if (col.formatter) {
        value = col.formatter(value, row)
      }
      // 处理包含逗号或换行符的值
      if (typeof value === 'string' && (value.includes(',') || value.includes('\n') || value.includes('"'))) {
        value = `"${value.replace(/"/g, '""')}"`
      }
      return value ?? ''
    })
    csvContent += values.join(',') + '\n'
  })

  // 创建 Blob 并下载
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${filename}_${new Date().toISOString().split('T')[0]}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 导出数据为 Excel 文件 (简化版，使用 CSV 格式但扩展名为 .xlsx)
 * 注意：这是 CSV 格式的伪装，真正的 Excel 需要使用第三方库如 xlsx
 */
export function exportToExcel(options: ExportOptions): void {
  exportToCSV({ ...options, filename: (options.filename || 'export') + '_excel' })
}

/**
 * 导出数据为 JSON 文件
 */
export function exportToJSON(options: ExportOptions): void {
  const { filename = 'export', data } = options

  const jsonContent = JSON.stringify(data, null, 2)
  const blob = new Blob([jsonContent], { type: 'application/json;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${filename}_${new Date().toISOString().split('T')[0]}.json`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 打印数据（表格格式）
 */
export function printData(options: ExportOptions): void {
  const { title = '数据导出', columns, data } = options

  // 创建打印窗口
  const printWindow = window.open('', '_blank')
  if (!printWindow) return

  // 构建打印内容
  let html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <title>${title}</title>
      <style>
        body { font-family: Arial, sans-serif; padding: 20px; }
        h1 { text-align: center; margin-bottom: 20px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f5f5f5; font-weight: bold; }
        tr:nth-child(even) { background-color: #f9f9f9; }
        @media print {
          body { padding: 0; }
        }
      </style>
    </head>
    <body>
      <h1>${title}</h1>
      <table>
        <thead>
          <tr>
            ${columns.map(col => `<th>${col.label}</th>`).join('')}
          </tr>
        </thead>
        <tbody>
          ${data.map(row => `
            <tr>
              ${columns.map(col => {
                let value = row[col.key]
                if (col.formatter) {
                  value = col.formatter(value, row)
                }
                return `<td>${value ?? ''}</td>`
              }).join('')}
            </tr>
          `).join('')}
        </tbody>
      </table>
      <p style="text-align: center; margin-top: 20px; color: #666;">
        打印时间: ${new Date().toLocaleString('zh-CN')}
      </p>
    </body>
    </html>
  `

  printWindow.document.write(html)
  printWindow.document.close()
  printWindow.print()
}
