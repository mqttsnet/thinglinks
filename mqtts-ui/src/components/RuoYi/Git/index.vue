<template>
  <div>
    <svg-icon icon-class="github" @click="goto"/>
  </div>
</template>

<script>
export default {
  name: 'mqttsGit',
  data() {
    return {
      url: 'https://gitee.com/xiaonnanet/mqtts'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
