import Vue from 'vue'
import BootstrapVue, { bTabs, bTab } from 'bootstrap-vue'
import App from './App.vue'
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap-vue/dist/bootstrap-vue.css';

Vue.config.productionTip = false

Vue.use(BootstrapVue)

new Vue({
  render: h => h(App),
  components: {
    bTab,
    bTabs
  }
}).$mount('#app')
