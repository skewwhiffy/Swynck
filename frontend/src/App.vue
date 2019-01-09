<template>
  <div class="App">
    <HeaderBar></HeaderBar>
    <div class="content">
      <b-tabs>
        <b-tab title="Files" active>
          <Items></Items>
        </b-tab>
        <b-tab title="Music">
          <br>MUSIC
        </b-tab>
      </b-tabs>
    </div>
  </div>
</template>

<script>
import HeaderBar from './components/HeaderBar.vue';
import Items from './components/items/Items.vue';

export default {
  name: 'app',
  components: {
    HeaderBar,
    Items
  }
}
</script>

<style scoped>
.content {
  max-width: 1000px;
  margin: auto;
}
.nav-tabs {
  margin-top: 15px;
}
</style>
