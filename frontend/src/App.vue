<template>
  <div class="App">
    <HeaderBar></HeaderBar>
    <div class="content">
      <b-tabs>
        <b-tab title="Files" active>
          <Items/>
        </b-tab>
        <b-tab title="Music">
          <Music/>
        </b-tab>
      </b-tabs>
    </div>
  </div>
</template>

<script>
import HeaderBar from './components/HeaderBar.vue';
import Items from './components/items/Items.vue';
import Music from './components/items/Music.vue';

export default {
  name: 'app',
  components: {
    HeaderBar,
    Items,
    Music
  }
}
</script>

<style scoped>
.content {
  max-width: 1000px;
  margin: auto;
}
.tabs {
  margin-top: 15px;
}
</style>
