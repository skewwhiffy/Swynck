<template>
  <div>
    <a href="#" @click="selectFolder">FOLDER: {{folder.name}}</a>
  </div>
</template>

<script>
export default {
  props: {
    folder: Object
  },
  name: 'folder',
  methods: {
    selectFolder() {
      this.$emit('change', this.folder);
    }
  }
}
</script>
