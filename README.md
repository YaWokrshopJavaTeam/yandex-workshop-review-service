## Review Service

### Endpoints
- `POST /reviews` - создать отзыв
- `PATCH /reviews/{reviewId}` - обновить отзыв (обновить может только автор (проверяем по header), нельзя обновить `authorId`, `createdDateTime`, `updatedDateTime`, `eventId`)
- `GET /reviews/{reviewId}` - получить отзыв по `id` (вернуть без `authorId`)
- `GET /reviews?page={page}&size={size}&eventId={eventId}` - получить отзывы с пагинацией и обязательно по заданному `id` события (вернуть без `authorId`)
- `DELETE /reviews/{reviewId}` - удалить отзыв (проверка по header, что удаляет автор)

### Models
Модель `Review` включает следующие поля: 
- id
- author
- title
- content
- createdOn
- updatedOn
- mark
- eventId

Модель `User` включает следующие поля: 
- id
- username