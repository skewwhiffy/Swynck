<template>
  <div class="App">
      <HeaderBar></HeaderBar>
      <div class="content">
          <p>TABS</p>
      </div>
  </div>
</template>

<script>
    import HeaderBar from './components/HeaderBar.vue';

    export default {
        name: 'app',
        components: {
            HeaderBar
        }
    }
</script>

<style scoped>
    .content {
        max-width: 1000px;
        margin: auto;
    }
</style>
