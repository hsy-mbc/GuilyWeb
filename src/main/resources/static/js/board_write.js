document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    const categoryBtns = document.querySelectorAll('.write-category .category');
    const selectedCategoryInput = document.getElementById('selectedCategory');
    const titleInput = document.querySelector('input[name="title"]');
    const contentInput = document.querySelector('textarea[name="content"]');
    const submitBtn = document.getElementById('submitBtn');

    categoryBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            categoryBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');

            const val = this.getAttribute('data-value');
            selectedCategoryInput.value = val;
        });
    });

    function validateForm() {
        const isTitleValid = titleInput.value.trim().length > 0;
        const isContentValid = contentInput.value.trim().length > 0;

        if (isTitleValid && isContentValid) {
            submitBtn.disabled = false;
            submitBtn.classList.add('active');
        } else {
            submitBtn.disabled = true;
            submitBtn.classList.remove('active');
        }
    }

    titleInput.addEventListener('input', validateForm);
    contentInput.addEventListener('input', validateForm);

    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        const formData = {
            category: selectedCategoryInput.value,
            title: titleInput.value,
            content: contentInput.value
        };

        try {
            submitBtn.disabled = true;
            submitBtn.innerText = "저장 중...";

            await apiRequest('/api/post', {
                method: 'POST',
                body: JSON.stringify(formData)
            });

            alert("글이 성공적으로 등록되었습니다.");
            location.href = '/board';

        } catch (error) {
            console.error('Write Error:', error);
            submitBtn.disabled = false;
            submitBtn.innerText = "작성 완료";
        }
    });
});