<template>
  <q-page class="column items-center">

    <div  style="padding-top:30px;max-width: 500px; min-width: 300px"  v-if="showLogin">

      <q-form

        class="q-gutter-md "
      >
        <q-input
          filled
          v-model="username"
          label="Username"
          hint="Your MarkLogic username and password"
          lazy-rules
          :rules="[ val => val && val.length > 0 || 'Please type something']"
        />

        <q-input
          filled
          type="password"
          v-model="password"
          label="Password"
        />


        <div>
          <q-btn label="Login" type="submit" color="primary" @click="loginToPipes" />
          <q-btn label="Reset" type="reset" color="primary" flat class="q-ml-sm" @click="function(){this.username=''; this.password=''}.bind(this)" />

        </div>
      </q-form>

    </div>
  </q-page>
</template>

<script>
export default {
  // name: 'PageName',
  data () {
    return {
      username:"",
      showLogin:false,
      password:"",
      loginmessage:""
    }
  },
  methods: {

    loginToPipes() {

      let payload = {"username":this.username, "password": this.password}
      this.$axios.post('/login', payload).then(response => {

        this.$root.$emit("logIn");
        this.$router.push({path:"/home"})
      })
        .catch((error) => {
          this.$q.notify({
            color: 'negative',
            position: 'center',
            message: "Login failed",
            icon: 'info',
            timeout: 800
          })
        })


    }
    },
  beforeCreate() {
  this.$axios.get('/status').then(response => {

    if(response.data && response.data.authenticated) {
      this.$root.$emit("logIn");
      this.$router.push({path: "/home"})
    }
    else this.showLogin=true
  })

},
  mounted: function () {



  }
}
</script>
