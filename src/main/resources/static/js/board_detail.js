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
                const originalText = commentSubmitBtn.innerText;
                commentSubmitBtn.innerText = "작성 중...";

                await apiRequest('/api/comments', {
                    method: 'POST',
                    body: JSON.stringify({
                        postNo: postNo,
                        content: content
                    })
                });

                commentInput.value = '';
                alert("댓글이 등록되었습니다.");
                location.reload();

            } catch (error) {
                console.error('Comment Error:', error);
                commentSubmitBtn.disabled = false;
                commentSubmitBtn.innerText = "작성";
            }
        });
    }


    const likeBtn = document.getElementById('btn-like');
    if (likeBtn) {
        likeBtn.addEventListener('click', async function() {
            const postNo = this.getAttribute('data-post-no');

            try {
                const count = await apiRequest(`/api/board/${postNo}/like`, {
                    method: 'POST'
                });

                const countElement = document.querySelector('.like-count');
                if (countElement) countElement.innerText = count;

                alert("좋아요가 반영되었습니다!");
            } catch (err) {
                console.error('Like Error:', err);
            }
        });
    }
});