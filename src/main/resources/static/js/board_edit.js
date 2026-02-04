document.addEventListener('DOMContentLoaded', function() {
    const categoryBtns = document.querySelectorAll('.category');
    const selectedCategoryInput = document.getElementById('selectedCategory');

    categoryBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            categoryBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            selectedCategoryInput.value = this.getAttribute('data-value');
        });
    });


    const editForm = document.getElementById('editForm');
    const postNo = document.getElementById('postNo').value;

    editForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const formData = new FormData(this);
        const category = selectedCategoryInput.value;

        if (!category) {
            alert('카테고리를 선택해주세요.');
            return;
        }

        const data = {
            category: category,
            title: formData.get('title'),
            content: formData.get('content')
        };

        try {
            await apiRequest(`/api/board/${postNo}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });

            alert('수정되었습니다.');
            location.href = `/board/${postNo}`;
        } catch (err) {
            console.error('Update Error:', err);
            alert('수정에 실패했습니다.');
        }
    });

});