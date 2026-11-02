# Guía de control de versiones (Git + GitHub)

## Estado actual del repositorio

- `main`: rama principal con la versión actual del proyecto APF3.
- `feature/acceso-invitado`: rama de desarrollo utilizada para implementar el flujo de acceso como invitado y recuperación de la reserva.
- Los cambios fueron registrados mediante commits descriptivos y posteriormente integrados a `main` mediante el Pull Request #1.

Para revisar el historial:

```bash
git log --oneline --graph --all --decorate
```

## Publicación en GitHub

1. Crear el repositorio en GitHub.
2. Configurar el repositorio remoto:

```bash
git remote add origin https://github.com/leoqz2004/peluqueria-bella-estilo.git
```

3. Publicar las ramas:

```bash
git push -u origin main
git push -u origin feature/acceso-invitado
```

4. Crear un Pull Request de `feature/acceso-invitado` hacia `main`.
5. Revisar los cambios y realizar el merge.
6. Actualizar la rama principal local:

```bash
git checkout main
git pull
```

## Convenciones utilizadas

- Rama principal: `main`.
- Ramas de funcionalidad: `feature/<nombre>`.
- Ramas de corrección: `fix/<nombre>`.
- Commits descriptivos utilizando prefijos como:
  - `feat:` para nuevas funcionalidades.
  - `fix:` para correcciones.
  - `docs:` para documentación.
  - `build:` para configuración y dependencias.
  - `test:` para pruebas.

## Buenas prácticas de seguridad

- No subir contraseñas, credenciales ni datos sensibles al repositorio.
- Mantener fuera del repositorio los archivos locales de configuración que contengan credenciales.
- No subir las carpetas `logs/` ni `target/`.
- Utilizar variables de entorno para las credenciales de la base de datos.
- Mantener un archivo `.gitignore` actualizado.

## Estado de la entrega

El proyecto es de desarrollo individual. El repositorio contiene el código fuente, documentación, historial de cambios, rama de desarrollo y evidencia del proceso de integración mediante Pull Request.