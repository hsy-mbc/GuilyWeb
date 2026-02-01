document.addEventListener('DOMContentLoaded', function() {

    const categoryButtons = document.querySelectorAll('.category');
    const listContainer = document.getElementById('post-list');

    categoryButtons.forEach(tab => {
        tab.addEventListener('click', async function() {
            const category = this.getAttribute('data-category');

            categoryButtons.forEach(t => t.classList.remove('active'));
            this.classList.add('active');

            let url = '/api/board/filter';
            if (category && category !== 'ALL') {
                url += `?category=${category}`;
            }

            try {
                const data = await apiRequest(url);

                listContainer.innerHTML = '';

                if (!data || data.length === 0) {
                    listContainer.innerHTML = `
                        <div style="text-align: center; padding: 80px 0; color: #999; width: 100%;">
                            <p style="font-size: 18px; margin-bottom: 8px;">게시글이 존재하지 않습니다. 📭</p>
                            <p style="font-size: 14px; color: #bbb;">이 카테고리의 첫 번째 주인공이 되어보세요!</p>
                        </div>`;
                    return;
                }

                data.forEach(post => {
                    const postHtml = `
                        <article class="board-item">
                            <div class="board-item-header">
                                <div class="profile">
                                    <span class="avatar"></span>
                                    <div>
                                        <span class="nickname">${post.authorName}</span>
                                        <span class="time">${post.timeAgo}</span>
                                    </div>
                                </div>
                                <span class="badge ${post.categoryCode ? post.categoryCode.toLowerCase() : ''}">
                                    ${post.categoryName}
                                </span>
                            </div>

                            <h3 class="board-item-title">
                                <a href="/board/${post.postNo}">${post.title}</a>
                            </h3>

                            <p class="board-item-content">${post.content}</p>

                            <div class="board-item-footer">
                                <span class="stat-item">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon-heart">
                                        <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/>
                                    </svg>
                                    ${post.likeCount}
                                </span>
                                <span class="stat-item">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon-comment">
                                        <path d="M7.9 20A9 9 0 1 0 4 16.1L2 22Z"/>
                                    </svg>
                                    ${post.commentCount}
                                </span>
                            </div>
                        </article>
                    `;
                    listContainer.insertAdjacentHTML('beforeend', postHtml);
                });

            } catch (err) {
                console.error("필터링 오류:", err);
                listContainer.innerHTML = '<div style="text-align:center; padding:50px; color:red;">데이터를 불러오는 중 오류가 발생했습니다.</div>';
            }
        });
    });
});