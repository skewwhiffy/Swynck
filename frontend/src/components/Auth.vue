<template>
  <div>
    <p v-if="loggedIn">{{loggedIn}}</p>
    <p v-if="loginRedirect">Login</p>
  </div>
</template>

<script>
import Api from '../service/Api';
const api = new Api();

export default {
  name: 'auth',
  data: function() {
    return {
      loginRedirect: false,
      loggedIn: false
    }
  }, 
  created: function() {
    const code = (new URL(window.location)).searchParams.get("code");
    if (code) {
      api
        .setAuthCode(code)
        .then(() => window.location.replace("/"));
    } else {
      api
        .getCurrentUser()
        .then(it => {
          if (it.redirect) {
            this.loginRedirect = it.redirect;
            this.loggedIn = false;
          }
          if (it.displayName) {
            this.loggedIn = it.displayName;
            this.loginRedirect = false;
          }
        });
    }
  }
}
</script>

<style scoped>
</style>
