<template>
  <div>
    <svg-icon icon-class="github" @click="goto"/>
  </div>
</template>

<script>
export default {
  name: 'MqttsGit',
  data() {
    return {
      url: 'https://github.com/xiaonannet/mqtts.git'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
