# ============================================================
# GSTech Backend - Production Dockerfile
# ============================================================

FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

# Copy the complete backend source.
COPY . .

# The current project ZIP does not contain backend/assets.
# Create the directory so the production stage can always copy it.
# If the OFPPT template is added later, it will be included automatically.
RUN mkdir -p /app/assets

RUN npm run build

# ============================================================
# Production image
# ============================================================

FROM node:20-alpine AS production

WORKDIR /app

ENV NODE_ENV=production

COPY package*.json ./
RUN npm ci --omit=dev && npm cache clean --force

COPY --from=builder /app/dist ./dist
COPY --from=builder /app/assets ./assets

RUN mkdir -p /app/uploads

EXPOSE 3000

CMD ["node", "dist/main.js"]
