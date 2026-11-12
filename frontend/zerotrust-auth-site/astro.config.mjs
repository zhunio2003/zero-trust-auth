// @ts-check
/*
 * ============================================================
 *  CONFIGURACIÓN DE ASTRO + STARLIGHT
 * ============================================================
 *  - La PORTADA son páginas propias (src/pages/index.astro y es/index.astro).
 *  - Los DOCS los maneja Starlight, bajo /docs/ (inglés) y /es/docs/ (español).
 *    Sus archivos están en src/content/docs/docs/ y src/content/docs/es/docs/.
 * ============================================================
 */
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

export default defineConfig({
    // Oculta la barrita de herramientas de Astro que aparece en desarrollo (tapaba la portada)
    devToolbar: { enabled: false },

    integrations: [
        starlight({
            title: 'ZeroTrust Auth',
            logo: { src: './src/assets/logo.svg' },
            favicon: '/favicon.svg',

            /*
             * IDIOMAS. "root" = el idioma por defecto, SIN prefijo en la URL (inglés).
             * "es" = español, con prefijo /es/. Starlight agrega solo el selector de idioma.
             */
            defaultLocale: 'root',
            locales: {
                root: { label: 'English', lang: 'en' },
                es: { label: 'Español', lang: 'es' },
            },

            social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/zhunio2003/zero-trust-auth' }],

            // Nuestro CSS: colores de marca y fondo oscuro sobre el tema de Starlight
            customCss: ['./src/styles/starlight.css'],

            /*
             * Reemplazamos componentes de Starlight por los nuestros:
             *  - ThemeProvider: fuerza SIEMPRE el modo oscuro.
             *  - ThemeSelect: vacío → desaparece el botón claro/oscuro.
             */
            components: {
                ThemeProvider: './src/components/starlight/ThemeProvider.astro',
                ThemeSelect: './src/components/starlight/ThemeSelect.astro',
            },

            // Fuentes (las mismas de la portada)
            head: [
                { tag: 'link', attrs: { rel: 'preconnect', href: 'https://fonts.googleapis.com' } },
                { tag: 'link', attrs: { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: true } },
                {
                    tag: 'link',
                    attrs: {
                        rel: 'stylesheet',
                        href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap',
                    },
                },
            ],

            /*
             * MENÚ LATERAL de los docs.
             *  - "label" + "translations": el nombre del GRUPO en cada idioma.
             *  - items con solo el slug (un string): Starlight usa el TÍTULO de
             *    la página en el idioma actual. Así no traducimos dos veces:
             *    el título ya está traducido en el frontmatter de cada archivo.
             *  - El slug es la ruta del archivo sin extensión, desde src/content/docs/
             *    (sin el "es/": Starlight lo agrega solo para español).
             */
            sidebar: [
                {
                    label: 'Introduction',
                    translations: { es: 'Introducción' },
                    items: ['docs', 'docs/getting-started'],
                },
                {
                    label: 'Guides',
                    translations: { es: 'Guías' },
                    items: ['docs/guides/authentication', 'docs/guides/passkeys', 'docs/guides/tokens'],
                },
                {
                    label: 'Reference',
                    translations: { es: 'Referencia' },
                    items: ['docs/reference/api'],
                },
                {
                    label: 'Security',
                    translations: { es: 'Seguridad' },
                    items: ['docs/security/overview', 'docs/security/threat-model'],
                },
                {
                    label: 'Project',
                    translations: { es: 'Proyecto' },
                    items: ['docs/project/architecture', 'docs/project/roadmap'],
                },
            ],
        }),
    ],
});
