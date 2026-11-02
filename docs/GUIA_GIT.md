# Guía de control de versiones (Git + GitHub)

## Estado actual del repositorio
- `main`: versión base recibida (APF2).
- `feature/acceso-invitado`: todos los cambios del Avance 3, en commits pequeños y con prefijo `feat:`, `build:`, `docs:`, `test:`.

```bash
git log --oneline --graph --all
```

## Publicar en GitHub (una sola vez)
1. Crear un repositorio **vacío** en GitHub (sin README).
2. En la carpeta del proyecto:
```bash
git remote add origin https://github.com/<usuario>/peluqueria-citas.git
git push -u origin main
git push -u origin feature/acceso-invitado
```
3. En GitHub, abrir un **Pull Request** de `feature/acceso-invitado` → `main`, revisar entre integrantes y hacer *merge*.

## Reglas del equipo
- Un **responsable de control de versiones** aprueba los Pull Requests.
- Una rama por historia de usuario: `feature/<nombre>`; correcciones: `fix/<nombre>`.
- Commits pequeños y descriptivos: `feat: ...`, `fix: ...`, `docs: ...`, `test: ...`.
- Nunca subir contraseñas ni `application-local.properties` (ya está en `.gitignore`), ni `logs/` ni `target/`.
- Cada integrante debe tener commits propios: es la evidencia de **autoría** que pide la rúbrica.
