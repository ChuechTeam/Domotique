// Include styles to be used globally throughout the app
import './assets/main.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'primeicons/primeicons.css' // https://primevue.org/icons/


import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config';
import Aura from '@primeuix/themes/aura';
import { Dialog, FloatLabel, InputText, Select, Button, InputNumber, Tag, Chip, Card, Textarea, ProgressSpinner, Message } from 'primevue';
import { ToastService } from 'primevue';

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(PrimeVue, {
    theme: {
        preset: Aura,
        options: {
            // Light theme only. Old people can't read dark themes properly anyway.
            darkModeSelector: false
        }
    }
})

app.use(ToastService)

app.component('Dialog', Dialog)
app.component('FloatLabel', FloatLabel)
app.component('InputText', InputText)
app.component('InputNumber', InputNumber)
app.component('Select', Select)
app.component('Button', Button)
app.component('Tag', Tag)
app.component('Chip', Chip)
app.component('Card', Card)
app.component('Textarea', Textarea)
app.component('ProgressSpinner', ProgressSpinner)
app.component('Message', Message)

app.use(createPinia())
app.use(router)

app.mount('#app')
