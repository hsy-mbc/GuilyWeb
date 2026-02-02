document.addEventListener('DOMContentLoaded', function() {

    const commentSubmitBtn = document.getElementById('btn-comment-submit');
    const commentInput = document.getElementById('comment-input');

    if (commentSubmitBtn) {
        commentSubmitBtn.addEventListener('click', async function() {
            const content = commentInput.value;
            const postNo = document.getElementById('btn-like').getAttribute('data-post-no');

            if (!content.trim()) {
                alert("댓글 내용을 입력해주세요.");
                commentInput.focus();
                return;
            }

            try {
                commentSubmitBtn.disabled = true;
                commentSubmitBtn.innerText = "작성 중...";

                const newComment = await apiRequest('/api/comments', {
                    method: 'POST',
                    body: JSON.stringify({
                        postNo: postNo,
                        content: content
                    })
                });

                commentInput.value = '';
                addCommentToList(newComment);
                alert("댓글이 등록되었습니다.");

            } catch (error) {
                console.error('Comment Error:', error);
                alert("댓글 등록에 실패했습니다.");
            } finally {
                commentSubmitBtn.disabled = false;
                commentSubmitBtn.innerText = "작성";
            }
        });
    }

    function addCommentToList(comment) {
        const commentList = document.getElementById('comment-list');
        if (!commentList) return;

        const commentHtml = `
        <li class="comment">
            <div class="avatar gray"></div>
            <div class="comment-body">
                <div class="comment-header">
                    <strong>${escapeHtml(comment.authorName)}</strong>
                    <span>${comment.timeAgo}</span>
                </div>
                <p>${escapeHtml(comment.content)}</p>
                <span class="comment-like">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/>
                    </svg>
                    0
                </span>
            </div>
        </li>
    `;

        // 댓글 리스트의 맨 위에 추가
        commentList.insertAdjacentHTML('afterbegin', commentHtml);

        // 댓글 개수 업데이트
        updateCommentCount(1);
    }

    function updateCommentCount(increment) {
        const commentTitle = document.querySelector('.comment-section h4');
        if (commentTitle) {
            const match = commentTitle.textContent.match(/댓글\s*(\d+)/);
            if (match) {
                const currentCount = parseInt(match[1]) || 0;
                commentTitle.textContent = `댓글 ${currentCount + increment}`;
            }
        }
    }


    const likeBtn = document.getElementById('btn-like');
    if (likeBtn) {
        likeBtn.addEventListener('click', async function() {
            const postNo = this.getAttribute('data-post-no');

            try {
                const result = await apiRequest(`/api/board/${postNo}/like`, {
                    method: 'POST'
                });

                const countElement = document.querySelector('.like-count');
                if (countElement) countElement.innerText = result.count;

                // 좋아요 추가/취소 여부에 따라 다른 메시지
                if (result.isLiked) {
                    alert("좋아요를 눌렀습니다!");
                } else {
                    alert("좋아요를 취소했습니다.");
                }
            } catch (err) {
                console.error('Like Error:', err);
                alert("좋아요 처리 중 오류가 발생했습니다.");
            }
        });
    }
});