from django.shortcuts import render, redirect
from django.contrib.auth.forms import UserCreationForm, AuthenticationForm
from django.contrib.auth import login, logout

# Create your views here.

def signup_view(request):
    error = False
    if request.method == 'POST':
        form = UserCreationForm(request.POST)
        if form.is_valid():
            user = form.save()
            # log the user in
            login(request, user)
            return redirect('articles:home')
        else:
            error = True
    else:
        form = UserCreationForm()
    form = UserCreationForm();
    context = {
        'form': form,
        'error': error,
    }
    return render(request, 'nireaccount/signup.html', context)

def login_view(request):
    error = False
    if request.method == 'POST':
        form = AuthenticationForm(data = request.POST)
        if form.is_valid():
            #login the user
            user = form.get_user()
            login(request, user)
            if 'next' in request.POST:
                return redirect(request.POST.get('next'))
            return redirect('articles:home')
        else:
            error = True
    else:
        form = AuthenticationForm()
    context = {
        'form': form,
        'error': error,
    }
    return render(request, 'nireaccount/login.html', context)

def logout_view(request):
    if request.method == 'POST':
        logout(request)
        return redirect('articles:home')