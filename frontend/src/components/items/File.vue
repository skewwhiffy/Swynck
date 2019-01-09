<template>
  <div>
    <p>FILE: {{file.name}}</p>
  </div>
</template>

<script>
export default {
  props: {
    file: Object
  },
  name: 'file'
}
</script>
