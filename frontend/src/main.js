import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router';
import { createPinia } from 'pinia';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import './assets/main.css';

const app = createApp(App);

// Globally register all Element Plus icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
}

// Custom console.warn to filter out specific, non-critical warnings
const originalWarn = console.warn;
console.warn = (...args) => {
    // Filter out warnings that are not strings (often internal validation objects from UI libraries)
    if (typeof args[0] !== 'string') {
        return;
    }
    // Filter out specific string-based warnings
    if (args[0].includes('Invalid prop') || args[0].includes('Extraneous non-props attributes')) {
        return;
    }
    originalWarn(...args);
};

app.use(createPinia());
app.use(router);
app.use(ElementPlus);

app.mount('#app');
