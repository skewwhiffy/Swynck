<template>
  <div>
    <p>Breadcrumbs</p>
    <ul>
      <li
        :key="index"
        v-for="(pathSection, index) in pathSections"
      >
        {{ pathSection }}
      </li>
    </ul>
  </div>
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
