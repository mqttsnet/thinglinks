<template>
  <div>
    <svg-icon icon-class="github" @click="goto"/>
  </div>
</template>

<script>
export default {
  name: 'ThingLinksGit',
  data() {
    return {
      url: 'https://github.com/mqttsnnet/thinglinks.git'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
