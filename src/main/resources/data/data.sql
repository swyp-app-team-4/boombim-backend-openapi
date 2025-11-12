INSERT INTO congestion_levels (id, name, message)
VALUES (1, '여유', '사람이 몰려있을 가능성이 낮고 붐빔은 거의 느껴지지 않아요. 도보 이동이 자유로워요.')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO congestion_levels (id, name, message)
VALUES (2, '보통', '사람이 몰려있을 수 있지만 크게 붐비지는 않아요. 도보 이동에 큰 제약이 없어요.')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO congestion_levels (id, name, message)
VALUES (3, '약간 붐빔', '사람들이 몰려있을 가능성이 크고 붐빈다고 느낄 수 있어요. 인구 밀도가 높은 구간에서는 도보 이동 시 부딪힘이 발생할 수 있어요.')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO congestion_levels (id, name, message)
VALUES (4, '붐빔', '사람들이 몰려있을 가능성이 매우 크고 많이 붐빈다고 느낄 수 있어요. 인구 밀도가 높은 구간에서는 도보 이동 시 부딪힘이 발생할 수 있어요.')
ON DUPLICATE KEY UPDATE id = id;
