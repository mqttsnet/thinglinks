<template>
  <div>
    <svg-icon icon-class="question" @click="goto"/>
  </div>
</template>

<script>
export default {
  name: 'ThingLinksDoc',
  data() {
    return {
      url: 'http://doc.thinglinks.mqttsnet.com'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
