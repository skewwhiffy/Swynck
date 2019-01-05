<template>
    <div class="header">
        <div class="headerContent">
            <div class="headerLeft">
                Swync
            </div>
            <div class="headerRight">
                <Auth></Auth>
            </div>
        </div>
    </div>
</template>

<script>
    import Auth from './Auth.vue';

    export default {
        name: 'headerBar',
        components: {
            Auth
        }
    }
</script>

<style scoped>
    .header {
        height: 70px;
        background-color: blueviolet;
        color: yellow;
        font-family: 'Lucida Sans', 'Lucida Sans Regular', 'Lucida Grande', 'Lucida Sans Unicode', Geneva, Verdana, sans-serif
    }

    .header .headerContent {
        margin: auto;
        border: 3px;
        padding: 10px;
        width: 350px;
    }

    .header .headerContent .headerLeft {
        left: 30px;
        position: absolute;
    }

    .header .headerContent .headerRight {
        right: 30px;
        position: absolute;
    }
</style>