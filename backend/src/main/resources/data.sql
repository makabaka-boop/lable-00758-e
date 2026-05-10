-- Init videos
INSERT IGNORE INTO videos (id, title, description, video_url, user_id, like_count, favorite_count, comment_count, view_count, created_at) VALUES
(1, '大自然风光', '美丽的自然风景', 'https://www.w3schools.com/html/mov_bbb.mp4', 1, 0, 0, 0, 0, NOW()),
(2, '城市夜景', '繁华都市的夜晚', 'https://www.w3schools.com/html/movie.mp4', 1, 0, 0, 0, 0, NOW()),
(3, '大象之梦', '开源动画短片', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', 1, 0, 0, 0, 0, NOW());
