<template>
  <div>
    <div>
      <audio v-if="false" controls>
        <source src="hello.mpg" type="audio/mpeg"/>
      </audio>
    </div>
    <div>
      <File :key="getKeyFromName(file.name)" v-for="file in files" :file="file"/>
    </div>
  </div>
</template>

<script>
import Api from '../../service/Api.js';
import File from './File.vue';
import md5 from 'md5';

const api = new Api();

export default {
  created() {
    this.refreshMusic();
  },
  name: 'music',
  components: {
    File
  },
  data() {
    return {
      files: []
    }
  },
  methods: {
    refreshMusic() {
      api
        .searchMusic()
        .then(it => this.files = it.files)
    },
    getKeyFromName: md5
  }
}
</script>
