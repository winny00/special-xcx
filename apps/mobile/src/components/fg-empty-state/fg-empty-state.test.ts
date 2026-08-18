import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import FgEmptyState from './fg-empty-state.vue'

describe('fg-empty-state', () => {
  it('用碳图标而不是「空」字', () => {
    const wrapper = mount(FgEmptyState, { props: { title: '暂无内容' } })
    expect(wrapper.html()).toContain('i-carbon-document-blank')
    expect(wrapper.text()).not.toMatch(/^\s*空/)
  })

  it('描述使用 muted，操作按钮至少 44px 高', () => {
    const wrapper = mount(FgEmptyState, {
      props: {
        title: '暂无推荐资源',
        description: '可以先浏览全部资源',
        actionText: '浏览全部资源',
      },
    })
    expect(wrapper.html()).toContain('text-muted')
    const action = wrapper.findAll('view').find(n => n.text() === '浏览全部资源')
    expect(action).toBeTruthy()
    expect(action!.classes().join(' ')).toContain('min-h-11')
  })
})
