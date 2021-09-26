<template>
  <div>
    <svg-icon icon-class="question" @click="goto"/>
  </div>
</template>

<script>
export default {
  name: 'MqttsDoc',
  data() {
    return {
      url: 'http://doc.mqtts.net/mqtts'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
