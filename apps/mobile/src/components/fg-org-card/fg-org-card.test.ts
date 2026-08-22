import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import FgOrgCard from './fg-org-card.vue'

const org = {
  id: '1',
  name: '阳光特教中心',
  orgType: 'center',
  coverUrl: 'https://winny.oss-cn-beijing.aliyuncs.com/special/org.png',
}

describe('fg-org-card', () => {
  it('封面用明确 88px 宽高，不用 h-full', () => {
    const wrapper = mount(FgOrgCard, { props: { org, typeLabel: '机构' } })
    const image = wrapper.find('image')
    expect(image.exists()).toBe(true)
    expect(image.classes().join(' ')).toContain('cover-image')
    expect(image.classes().join(' ')).not.toContain('h-full')
    expect(wrapper.html()).toContain('width: 88px')
    expect(wrapper.html()).toContain('height: 88px')
  })

  it('封面加载失败时回退到名称首字', async () => {
    const wrapper = mount(FgOrgCard, { props: { org, typeLabel: '机构' } })
    await wrapper.find('image').trigger('error')
    expect(wrapper.find('image').exists()).toBe(false)
    expect(wrapper.text()).toContain('阳')
  })
})
