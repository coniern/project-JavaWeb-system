export const projectStatusOptions = [
  { label: '待规划', value: 'planning', tag: 'info' },
  { label: '进行中', value: 'developing', tag: 'warning' },
  { label: '待审核', value: 'testing', tag: 'success' },
  { label: '已完成', value: 'deployed', tag: 'success' }
]

export const techStackOptions = [
  'Spring Boot',
  'Spring Boot + Vue 3',
  'Spring Boot + MyBatis-Plus',
  'Spring Boot + Spring Security',
  'Spring Boot + AI'
]

export const taskStatusOptions = [
  { label: '待开始', value: 'todo', tag: 'info' },
  { label: '进行中', value: 'in_progress', tag: 'warning' },
  { label: '已完成', value: 'done', tag: 'success' }
]

export const taskPriorityOptions = [
  { label: '高', value: 1, tag: 'danger' },
  { label: '中', value: 2, tag: 'warning' },
  { label: '低', value: 3, tag: 'info' }
]

export const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '开发人员', value: 'DEVELOPER' },
  { label: '测试人员', value: 'TESTER' }
]

export function findOptionLabel(options, value, fallback = '') {
  return options.find((item) => item.value === value)?.label || fallback
}

export function findOptionTag(options, value, fallback = 'info') {
  return options.find((item) => item.value === value)?.tag || fallback
}
