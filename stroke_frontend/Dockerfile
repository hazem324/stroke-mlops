# Stage 1 - Angular build
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./

RUN npm ci --legacy-peer-deps

COPY . .

RUN npm run build

# Stage 2 - Nginx
FROM nginx:alpine

RUN rm -f /usr/share/nginx/html/*

COPY --from=builder /app/dist/stroke_frontend/browser /usr/share/nginx/html

RUN rm -f /etc/nginx/conf.d/default.conf

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]