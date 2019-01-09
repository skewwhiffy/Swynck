<template>
  <div>
    <p>FOLDER: {{folder.name}}</p>
  </div>
</template>

<script>
export default {
  props: {
    folder: Object
  },
  name: 'folder'
}
</script>
