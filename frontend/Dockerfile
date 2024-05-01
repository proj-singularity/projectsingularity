# Development Build

FROM node:18-alpine as builder
WORKDIR /ps_app
COPY ./package*.json /ps_app
RUN npm install
COPY . .
RUN npm run build


# Production Build

FROM nginx:1.21.3-alpine
WORKDIR /usr/share/nginx/html
RUN rm -rf ./*
COPY --from=builder /ps_app/dist .
ENTRYPOINT ["nginx", "-g", "daemon off;"]