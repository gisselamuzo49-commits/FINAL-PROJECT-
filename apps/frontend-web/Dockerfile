# Etapa 1: Construcción
FROM node:20-alpine AS build
WORKDIR /app

# Argumentos para pasar los puertos de backend
ARG VITE_AUTH_PORT
ARG VITE_INTERNSHIP_PORT
ENV VITE_AUTH_PORT=$VITE_AUTH_PORT
ENV VITE_INTERNSHIP_PORT=$VITE_INTERNSHIP_PORT

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Etapa 2: Servidor web ligero Nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
