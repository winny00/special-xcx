import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import FgResourceCard from './fg-resource-card.vue'

const item = {
  id: '1',
  title: '儿童应对方法',
  resourceType: 'course',
  coverUrl: 'https://winny.oss-cn-beijing.aliyuncs.com/special/a.png',
}

describe('fg-resource-card', () => {
  it('封面用明确 88px 宽高，不用 h-full', () => {
    const wrapper = mount(FgResourceCard, { props: { item } })
    const image = wrapper.find('image')
    expect(image.exists()).toBe(true)
    expect(image.classes().join(' ')).toContain('cover-image')
    expect(image.classes().join(' ')).not.toContain('h-full')
    expect(wrapper.html()).toContain('width: 88px')
    expect(wrapper.html()).toContain('height: 88px')
  })

  it('无封面时显示类型字', () => {
    const wrapper = mount(FgResourceCard, {
      props: { item: { ...item, coverUrl: undefined } },
    })
    expect(wrapper.find('image').exists()).toBe(false)
    expect(wrapper.text()).toContain('课')
  })

  it('封面加载失败时回退到类型字', async () => {
    const wrapper = mount(FgResourceCard, { props: { item } })
    await wrapper.find('image').trigger('error')
    expect(wrapper.find('image').exists()).toBe(false)
    expect(wrapper.text()).toContain('课')
  })
})
