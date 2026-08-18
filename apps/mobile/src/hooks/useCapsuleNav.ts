import { computed, ref } from 'vue'

/**
 * 自定义顶栏安全区。小程序会避开右上角胶囊，H5 只用状态栏高度。
 */
export function useCapsuleNav() {
  const statusBarHeight = ref(0)
  const capsuleHeight = ref(32)
  const capsuleRight = ref(0)

  try {
    const windowInfo = uni.getSystemInfoSync()
    statusBarHeight.value = windowInfo.statusBarHeight || 0

    const getMenuRect = (uni as any).getMenuButtonBoundingClientRect
    if (typeof getMenuRect === 'function') {
      const menu = getMenuRect.call(uni)
      if (menu && menu.width) {
        statusBarHeight.value = menu.top
        capsuleHeight.value = menu.height
        capsuleRight.value = (windowInfo.windowWidth || 0) - menu.left + 8
      }
    }
  }
  catch (e) {
    console.warn('读取胶囊位置失败', e)
  }

  const headerPaddingStyle = computed(() => {
    return {
      paddingTop: `${statusBarHeight.value}px`,
    }
  })

  const capsuleRowStyle = computed(() => {
    return {
      minHeight: `${capsuleHeight.value}px`,
      paddingRight: `${capsuleRight.value}px`,
    }
  })

  return {
    statusBarHeight,
    capsuleHeight,
    capsuleRight,
    headerPaddingStyle,
    capsuleRowStyle,
  }
}
