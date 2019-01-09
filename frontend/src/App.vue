<template>
  <div class="App">
    <HeaderBar></HeaderBar>
    <div class="content">
      <b-card no-body>
        <b-tabs card v-model="tabIndex" @input="setTabValue">
          <b-tab title="Files">
            <Items/>
          </b-tab>
          <b-tab title="Music">
            <Music/>
          </b-tab>
        </b-tabs>
      </b-card>
    </div>
  </div>
</template>

<script>
import HeaderBar from './components/HeaderBar.vue';
import Items from './components/items/Items.vue';
import Music from './components/items/Music.vue';
import Router from './service/Router.js';

const router = new Router();
const tabValues = ["files", "music"]

export default {
  name: 'app',
  components: {
    HeaderBar,
    Items,
    Music
  },
  data() {
    return {
      tabIndex: 0
    };
  },
  created() {
    this.tabIndex = this.getTabValue();
  },
  methods: {
    getTabValue() {
      return tabValues.indexOf(router.getTab());
    },
    setTabValue(value) {
      router.setTab(tabValues[value]);
    }
  }
}
</script>

<style scoped>
.content {
  max-width: 1000px;
  margin: auto;
}
.content {
  margin-top: 15px;
}
</style>
