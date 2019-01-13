<template>
  <p>Breadcrumbs {{ pathSections }}!!</p>
</template>

<script>
export default {
  props: {
    pathSections: {
      type: Array,
      required: true
    }
  },
  name: 'breadcrumbs'
}
</script>
